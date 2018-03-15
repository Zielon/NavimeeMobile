package org.pl.android.drively.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.eventbus.HotspotSettingsChanged;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import timber.log.Timber;

public class GeolocationUpdateService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final long TIME_FOR_SERVICE = 1800000;
    public static String FIREBASE_KEY = "";
    private static String DRIVER_TYPE = "";
    private static KalmanFilterService KALMAN_FILTER = new KalmanFilterService();
    @Inject
    DataManager dataManager;
    private GeoFire geoFire;
    private DatabaseReference databaseReference;
    private LocationManager mLocationManager = null;
    private User user;
    private LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.e("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
        EventBus.getDefault().register(this);
        databaseReference = dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION);
        geoFire = new GeoFire(databaseReference);
        initializeLocationManager();
        final Handler handler = new Handler();
        handler.postDelayed(this::stopSelf, TIME_FOR_SERVICE);

        user = dataManager.getPreferencesHelper().getUserInfo();

        DRIVER_TYPE = user.getDriverType();
        FIREBASE_KEY = DRIVER_TYPE + "_" + user.getId();

        if (!DRIVER_TYPE.isEmpty() && user.isShareLocalization()) {
            startLocationUpdates();
        }
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        // Reset the covariance matrix.
        GeolocationUpdateService.KALMAN_FILTER = new KalmanFilterService();

        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty())
            databaseReference.child(FIREBASE_KEY).removeValue();

        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners)
                mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @SuppressLint("TimberArgCount")
    private void startLocationUpdates() {
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Timber.e("fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Timber.d("network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Timber.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Timber.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Subscribe
    public void onDriverTypeChanged(HotspotSettingsChanged hotspotSettingsChanged) {
        DRIVER_TYPE = hotspotSettingsChanged.getDriverType();

        if (FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty()) {
            databaseReference.child(FIREBASE_KEY).removeValue();
        }

        FIREBASE_KEY = DRIVER_TYPE + "_" + user.getId();

        if (hotspotSettingsChanged.getShareLocalization()) {
            startLocationUpdates();
        } else {
            stopSelf();
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
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
    }
}