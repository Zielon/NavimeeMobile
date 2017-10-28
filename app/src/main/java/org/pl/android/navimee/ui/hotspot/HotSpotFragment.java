package org.pl.android.navimee.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.main.MainActivity;
import org.pl.android.navimee.util.DetectedActivityToString;
import org.pl.android.navimee.util.DisplayTextOnViewAction;
import org.pl.android.navimee.util.LocationToStringFunc;
import org.pl.android.navimee.util.ToMostProbableActivity;
import org.reactivestreams.Subscription;
import org.xml.sax.ErrorHandler;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

import static com.facebook.FacebookSdk.getApplicationContext;
import static org.pl.android.navimee.util.RxUtil.dispose;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class HotSpotFragment extends Fragment  implements HotSpotMvpView{


    MapView mMapView;
    private GoogleMap googleMap;
    private ReactiveLocationProvider locationProvider;
    RxPermissions rxPermissions;
    private Observable<Location> locationUpdatesObservable;
    private Observable<Location> lastKnownLocationObservable;
    private Observable<ActivityRecognitionResult> activityObservable;

    private Disposable lastKnownLocationDisposable;
    private Disposable updatableLocationDisposable;
    private Disposable activityDisposable;

    private final static int REQUEST_CHECK_SETTINGS = 0;

    @Inject HotSpotPresenter mHotspotPresenter;

    public static HotSpotFragment newInstance() {
        HotSpotFragment fragment = new HotSpotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        initGeolocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hotspot_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mHotspotPresenter.attachView(this);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @SuppressLint("MissingPermission")
    private void initGeolocation() {
        rxPermissions = new RxPermissions(getActivity());
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        lastKnownLocationObservable = locationProvider
                .getLastKnownLocation()
                .observeOn(AndroidSchedulers.mainThread());
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(5)
                .setInterval(100);

        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                                .build()
                )
                .doOnNext(new Consumer<LocationSettingsResult>() {
                    @Override
                    public void accept(LocationSettingsResult locationSettingsResult) {
                        Status status = locationSettingsResult.getStatus();
                        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException th) {
                                Log.e("MainActivity", "Error opening settings activity.", th);
                            }
                        }
                    }
                })
                .flatMap(new Function<LocationSettingsResult, Observable<Location>>() {
                    @Override
                    public Observable<Location> apply(LocationSettingsResult locationSettingsResult) {
                        return locationProvider.getUpdatedLocation(locationRequest);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        activityObservable = locationProvider
                .getDetectedActivity(50)
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected void onLocationPermissionGranted() {
        lastKnownLocationDisposable = lastKnownLocationObservable
                .map(new Function<Location,LatLng>() {
                    @Override
                    public LatLng apply(Location s) {
                        return new LatLng(s.getLatitude(),s.getLongitude());
                    }
                })
                .subscribe(new Consumer<LatLng>() {
                    @Override
                    public void accept(LatLng latLng) throws Exception {
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                        googleMap.animateCamera(yourLocation);
                    }
                }, new ErrorHandler());

        updatableLocationDisposable = locationUpdatesObservable
                .map(new Function<Location,LatLng>() {
                    @Override
                    public LatLng apply(Location s) {
                        return new LatLng(s.getLatitude(),s.getLongitude());
                    }
                })
                .subscribe(new Consumer<LatLng>() {
                    @Override
                    public void accept(LatLng latLng) throws Exception {
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                        googleMap.animateCamera(yourLocation);
                    }
                }, new ErrorHandler());



        activityDisposable = activityObservable
                .map(new ToMostProbableActivity())
                .map(new DetectedActivityToString())
                .subscribe(new DisplayTextOnViewAction(), new ErrorHandler());
    }


    @Override
    public void onStart() {
        super.onStart();
        mMapView.onResume();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mHotspotPresenter.checkLogin() != null) {
            rxPermissions
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) { // Always true pre-M
                            onLocationPermissionGranted();
                            mMapView.getMapAsync(new OnMapReadyCallback() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;

                                    // For showing a move to my location button
                                    googleMap.setMyLocationEnabled(true);
                                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                                }
                            });
                        } else {
                            mMapView.getMapAsync(new OnMapReadyCallback() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;
                                }
                            });
                        }
                    });
        }
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        dispose(updatableLocationDisposable);
        dispose(lastKnownLocationDisposable);
        dispose(activityDisposable);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private class ErrorHandler implements Consumer<Throwable> {
        @Override
        public void accept(Throwable throwable) {
            Toast.makeText(getApplicationContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Error occurred", throwable);
        }
    }
}
