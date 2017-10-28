package org.pl.android.navimee.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import javax.inject.Inject;

import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class HotSpotFragment extends Fragment  implements HotSpotMvpView{


    MapView mMapView;
    private GoogleMap googleMap;
    private ReactiveLocationProvider locationProvider;
    RxPermissions rxPermissions;

    @Inject HotSpotPresenter mHotspotPresenter;

    public static HotSpotFragment newInstance() {
        HotSpotFragment fragment = new HotSpotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        rxPermissions = new RxPermissions(getActivity());
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
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
                            mMapView.getMapAsync(new OnMapReadyCallback() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;

                                    // For showing a move to my location button
                                    googleMap.setMyLocationEnabled(true);
                                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                                    // For dropping a marker at a point on the Map
                                    LatLng sydney = new LatLng(-34, 151);
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                                    // For zooming automatically to the location of the marker
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }
                            });
                        } else {
                            mMapView.getMapAsync(new OnMapReadyCallback() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;

                                    // For dropping a marker at a point on the Map
                                    LatLng sydney = new LatLng(-34, 151);
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                                    // For zooming automatically to the location of the marker
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
