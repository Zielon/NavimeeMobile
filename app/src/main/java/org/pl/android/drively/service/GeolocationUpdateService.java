package org.pl.android.drively.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.firebase.database.DatabaseReference;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by Wojtek on 2018-02-18.
 */

public class GeolocationUpdateService extends Service
{
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 10f;

    GeoFire geoFire;

    @Inject
    DataManager dataManager;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Timber.e("LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Timber.e("onLocationChanged: " + location);
            mLastLocation.set(location);
            geoFire.setLocation(FirebasePaths.USER_LOCATION+dataManager.getFirebaseService().getFirebaseAuth().getUid(),new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Timber.e("onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Timber.e("onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Timber.e("onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Timber.e("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void onCreate()
    {
        Timber.e("onCreate");
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
        DatabaseReference databaseReference = dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.HOTSPOT_CURRENT);
        geoFire = new GeoFire(databaseReference);
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Timber.e( "fail to request location update, ignore", ex);
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

    @SuppressLint("TimberArgCount")
    @Override
    public void onDestroy()
    {
        Timber.e("onDestroy");
        super.onDestroy();
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

    private void initializeLocationManager() {
        Timber.e("initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}