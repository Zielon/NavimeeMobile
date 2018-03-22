package org.pl.android.drively.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.text.format.DateUtils;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.database.DatabaseReference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.eventbus.HotspotSettingsChanged;
import org.pl.android.drively.data.model.eventbus.RestartServiceSignal;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

import static org.pl.android.drively.ui.main.MainActivity.getActivity;

public class GeoLocationUpdateService extends Service {

    private static final String TAG = "GeoLocationUpdateService".toUpperCase();
    private static final int LOCATION_DISPLACEMENT = 5;
    private static final long TIME_FOR_SERVICE_SHUTDOWN = DateUtils.HOUR_IN_MILLIS;
    public static String FIREBASE_KEY = "";
    @Inject
    DataManager dataManager;
    private KalmanFilterService kalmanFilter = new KalmanFilterService();
    private Handler handler;
    private GeoFire geoFire;
    private DatabaseReference databaseReference;
    private Runnable stopRunning;

    private Observable<Location> locationUpdatesObservable;
    private Disposable updatableLocationDisposable;

    public static void startService() {
        if (getActivity() == null) return;
        Intent intentGeoService = new Intent(getActivity(), GeoLocationUpdateService.class);
        getActivity().startService(intentGeoService);
    }

    public static void stopService() {
        if (getActivity() == null) return;
        Intent intentGeoService = new Intent(getActivity(), GeoLocationUpdateService.class);
        getActivity().stopService(intentGeoService);
    }

    public static boolean isServiceRunning(Activity activity) {
        if (activity == null) return false;
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GeoLocationUpdateService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        /* Ignore binding */
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
        EventBus.getDefault().register(this);

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        this.databaseReference = dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION);
        this.geoFire = new GeoFire(databaseReference);
        this.handler = new Handler(thread.getLooper());
        this.stopRunning = this::stopSelf;

        // Register runnables
        this.handler.postDelayed(this.stopRunning, TIME_FOR_SERVICE_SHUTDOWN);

        this.setFirebaseKey();

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(LOCATION_DISPLACEMENT)
                .setFastestInterval(DateUtils.SECOND_IN_MILLIS)
                .setInterval(DateUtils.SECOND_IN_MILLIS);

        this.locationUpdatesObservable = locationProvider
                .checkLocationSettings(new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build())
                .flatMap(settingsResult -> locationProvider.getUpdatedLocation(locationRequest)) /* The infinite stream of location updates */
                .subscribeOn(AndroidSchedulers.from(thread.getLooper()))
                .observeOn(AndroidSchedulers.from(thread.getLooper()));

        if (dataManager.getPreferencesHelper().getUserInfo().isShareLocalization())
            this.startLocationUpdates();
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        this.handler.removeCallbacksAndMessages(null);

        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty())
            databaseReference.child(FIREBASE_KEY).removeValue()
                    .addOnSuccessListener(success -> Timber.i(FIREBASE_KEY + " has been deleted!"))
                    .addOnFailureListener(Throwable::printStackTrace);

        if (updatableLocationDisposable != null)
            updatableLocationDisposable.dispose();
    }

    @SuppressLint("TimberArgCount")
    private void startLocationUpdates() {
        updatableLocationDisposable = locationUpdatesObservable
                .subscribe(location -> {
                    // On each location update refresh the delay session
                    handler.removeCallbacks(this.stopRunning);
                    handler.postDelayed(this.stopRunning, TIME_FOR_SERVICE_SHUTDOWN);

                    location = kalmanFilter.filter(location);
                    GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                    if (!FIREBASE_KEY.isEmpty())
                        geoFire.setLocation(FIREBASE_KEY, geoLocation, (key, error) -> { /* Ignore callback */ });
                });
    }

    @Subscribe
    public void restartServiceTimeout(RestartServiceSignal restartServiceSignal) {
        // On HotSpotFragment onResume() refresh the delay session
        handler.removeCallbacks(this.stopRunning);
        handler.postDelayed(this.stopRunning, TIME_FOR_SERVICE_SHUTDOWN);
        Timber.d("Restarting GeolocationService. Time set up: %d", TIME_FOR_SERVICE_SHUTDOWN);
    }

    @Subscribe
    public void onDriverTypeChanged(HotspotSettingsChanged hotspotSettingsChanged) {
        // Remove the old key from data base and then set a new data set
        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty())
            databaseReference.child(FIREBASE_KEY).removeValue();

        this.setFirebaseKey();

        if (hotspotSettingsChanged.getShareLocalization())
            this.startLocationUpdates();
        else
            this.stopSelf();
    }

    private void setFirebaseKey() {
        User user = dataManager.getPreferencesHelper().getUserInfo();
        String DRIVER_TYPE = user.getDriverType();
        FIREBASE_KEY = String.format("%s_%s", DRIVER_TYPE, user.getId());
    }
}