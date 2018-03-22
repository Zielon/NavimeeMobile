package org.pl.android.drively.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.text.format.DateUtils;

import com.annimon.stream.Stream;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.eventbus.HotspotSettingsChanged;
import org.pl.android.drively.data.model.eventbus.RestartServiceSignal;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import timber.log.Timber;

public class GeolocationUpdateService extends Service {

    @Inject
    DataManager dataManager;
    private static final String TAG = "GeolocationUpdateService".toUpperCase();
    private static final int LOCATION_INTERVAL = 800;
    private static final float LOCATION_DISTANCE = 2.5f;
    private static final long TIME_FOR_SERVICE_SHUTDOWN = DateUtils.HOUR_IN_MILLIS;
    public static String FIREBASE_KEY = "";
    private static KalmanFilterService KALMAN_FILTER = new KalmanFilterService();
    private Handler handler;
    private GeoFire geoFire;
    private DatabaseReference databaseReference;
    private LocationManager mLocationManager = null;
    private Runnable stopRunning;
    private Runnable updatesRunnable;
    private LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public static void startService() {
        if (MainActivity.getActivity() == null) return;
        Intent intentGeoService = new Intent(MainActivity.getActivity(), GeolocationUpdateService.class);
        MainActivity.getActivity().startService(intentGeoService);
    }

    public static void stopService() {
        if (MainActivity.getActivity() == null) return;
        Intent intentGeoService = new Intent(MainActivity.getActivity(), GeolocationUpdateService.class);
        MainActivity.getActivity().stopService(intentGeoService);
    }

    public static boolean isServiceRunning(Activity activity) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if(manager == null) return false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GeolocationUpdateService.class.getName().equals(service.service.getClassName())) {
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

    @SuppressLint("TimberArgCount")
    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
        EventBus.getDefault().register(this);

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        initializeLocationManager();
        this.databaseReference = dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION);
        this.geoFire = new GeoFire(databaseReference);
        this.handler = new Handler(thread.getLooper());
        this.stopRunning = this::stopSelf;
        this.updatesRunnable = this::startLocationUpdates;

        // Register runnables
        this.handler.postDelayed(this.stopRunning, TIME_FOR_SERVICE_SHUTDOWN);
        this.handler.post(this.updatesRunnable);

        this.setFirebaseKey();
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        // Reset the covariance matrix.
        GeolocationUpdateService.KALMAN_FILTER = new KalmanFilterService();

        this.handler.removeCallbacksAndMessages(null);

        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty())
            databaseReference.child(FIREBASE_KEY).removeValue()
                    .addOnSuccessListener(success -> Timber.i(FIREBASE_KEY + " has been deleted!"))
                    .addOnFailureListener(Throwable::printStackTrace);

        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners)
                mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @SuppressLint("TimberArgCount")
    private void startLocationUpdates() {
        Stream.of(mLocationListeners).forEach(listener -> {
            try {
                mLocationManager.requestLocationUpdates(listener.getProvider(), LOCATION_INTERVAL, LOCATION_DISTANCE, listener, this.handler.getLooper());
            } catch (java.lang.SecurityException ex) {
                Timber.e("Fail to request location update. %s", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                Timber.d("%s does not exist. %s", listener.getProvider(), ex.getMessage());
            }
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
        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty())
            databaseReference.child(FIREBASE_KEY).removeValue();

        setFirebaseKey();

        if (hotspotSettingsChanged.getShareLocalization()) {
            handler.removeCallbacks(this.updatesRunnable);
            handler.post(this.updatesRunnable);
        }
        else
            stopSelf();
    }

    private void initializeLocationManager() {
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    private void setFirebaseKey() {
        User user = dataManager.getPreferencesHelper().getUserInfo();
        String DRIVER_TYPE = user.getDriverType();
        FIREBASE_KEY = DRIVER_TYPE + "_" + user.getId();
    }

    private class LocationListener implements android.location.LocationListener {
        private Location mLastLocation;
        private String provider;

        public LocationListener(String provider) {
            this.provider = provider;
            this.mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            // On each location update refresh the delay session
            handler.removeCallbacks(GeolocationUpdateService.this.stopRunning);
            handler.postDelayed(GeolocationUpdateService.this.stopRunning, TIME_FOR_SERVICE_SHUTDOWN);
            Timber.d("Location has changed: %s", location.toString());

            location = KALMAN_FILTER.filter(location);
            mLastLocation.set(location);
            if (!FIREBASE_KEY.isEmpty())
                geoFire.setLocation(FIREBASE_KEY, new GeoLocation(location.getLatitude(), location.getLongitude()),
                        (locationKey, databaseError) -> {
                        });
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public String getProvider() {
            return provider;
        }
    }
}