package org.pl.android.drively.service;

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
import org.pl.android.drively.data.model.eventbus.CompanySelectedEvent;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import timber.log.Timber;

public class GeolocationUpdateService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final long TIME_FOR_SERVICE = 1800000;
    private static String USER_COMPANY;
    public static String FIREBASE_KEY;
    private DatabaseReference databaseReference;
    GeoFire geoFire;
    @Inject
    DataManager dataManager;
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private LocationManager mLocationManager = null;

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
        Timber.e("onCreate");
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
        EventBus.getDefault().register(this);
        databaseReference = dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION);
        geoFire = new GeoFire(databaseReference);
        initializeLocationManager();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Timber.i("stoping service");
                stopSelf();
            }
        }, TIME_FOR_SERVICE);
        USER_COMPANY = dataManager.getPreferencesHelper().getValueString(Const.USER_COMPANY);
        if(!USER_COMPANY.isEmpty()) {
            startLocationUpdates();
        }
    }


    @SuppressLint("TimberArgCount")
    @Override
    public void onDestroy() {
        Timber.e("onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(FIREBASE_KEY != null && !FIREBASE_KEY.isEmpty()) {
            databaseReference.child(FIREBASE_KEY).removeValue();
        }
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Timber.i("fail to remove location listners, ignore", ex);
                }
            }
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


    @Subscribe
    public void onEvent(CompanySelectedEvent companySelectedEvent) {
        USER_COMPANY = dataManager.getPreferencesHelper().getValueString(Const.USER_COMPANY);
        startLocationUpdates();
    }

    private void initializeLocationManager() {
        Timber.e("initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Timber.e("LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Timber.e("onLocationChanged: " + location);
            Timber.i("USER_COMPANY: " + USER_COMPANY);
            mLastLocation.set(location);
            FIREBASE_KEY = USER_COMPANY + "_" + FirebasePaths.USER_LOCATION + "_" + dataManager.getFirebaseService().getFirebaseAuth().getUid();
            geoFire.setLocation(FIREBASE_KEY,
                    new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Timber.e("onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Timber.e("onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Timber.e("onStatusChanged: " + provider);
        }
    }
}