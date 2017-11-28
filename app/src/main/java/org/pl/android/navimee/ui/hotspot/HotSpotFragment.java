package org.pl.android.navimee.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.util.AddressToStringFunc;
import org.pl.android.navimee.util.DetectedActivityToString;
import org.pl.android.navimee.util.DisplayTextOnViewAction;
import org.pl.android.navimee.util.ToMostProbableActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

import static com.facebook.FacebookSdk.getApplicationContext;
import static org.pl.android.navimee.util.RxUtil.dispose;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class HotSpotFragment extends Fragment  implements HotSpotMvpView, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener,
        AdapterView.OnItemSelectedListener,RoutingListener {


    MapView mMapView;
    private GoogleMap googleMap;
    private ReactiveLocationProvider locationProvider;
    RxPermissions rxPermissions;
    private Observable<Location> locationUpdatesObservable;
    private Observable<Location> lastKnownLocationObservable;
    private Observable<ActivityRecognitionResult> activityObservable;
    private Observable<String> addressObservable;

    private Disposable lastKnownLocationDisposable;
    private Disposable updatableLocationDisposable;
    private Disposable activityDisposable;
    private Disposable addressDisposable;

    private List<Polyline> polylines;
    LatLng latLngCurrent,latLngEnd;
    LatLng start;
    LatLng waypoint;
    LatLng end;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};


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
        polylines = new ArrayList<>();
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

        addressObservable = locationProvider.getUpdatedLocation(locationRequest)
                .flatMap(new Function<Location, Observable<List<Address>>>() {
                    @Override
                    public Observable<List<Address>> apply(Location location) {
                        return locationProvider.getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                    }
                })
                .map(new Function<List<Address>, Address>() {
                    @Override
                    public Address apply(List<Address> addresses) {
                        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
                    }
                })
                .map(new AddressToStringFunc())
                .subscribeOn(Schedulers.io())
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
                        //mock
                        LatLng latLng1 = new LatLng(latLng.latitude+0.04,latLng.longitude+0.05);
                        LatLng latLng2 = new LatLng(latLng.latitude-0.04,latLng.longitude-0.05);
                        googleMap.addCircle(new CircleOptions()
                                .center(latLng1)
                                .radius(2000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.RED))
                                .setClickable(true);

                        googleMap.addCircle(new CircleOptions()
                                .center(latLng2)
                                .radius(1500)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.RED))
                                .setClickable(true);
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
                        latLngCurrent = latLng;
                        //mock
                        LatLng latLng1 = new LatLng(latLng.latitude+0.04,latLng.longitude+0.05);
                        LatLng latLng2 = new LatLng(latLng.latitude-0.04,latLng.longitude-0.05);
                        LatLng latLng3 = new LatLng(latLng.latitude-0.07,latLng.longitude-0.09);
                        LatLng latLng4 = new LatLng(latLng.latitude+0.07,latLng.longitude-0.09);
                        googleMap.addCircle(new CircleOptions()
                                .center(latLng1)
                                .radius(1000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.RED))
                                .setClickable(true);

                        googleMap.addCircle(new CircleOptions()
                                .center(latLng2)
                                .radius(1500)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.RED))
                                .setClickable(true);

                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                        Bitmap bmp = Bitmap.createBitmap(80, 90, conf);
                        Bitmap bmp2 = Bitmap.createBitmap(80, 90, conf);
                        Canvas canvas1 = new Canvas(bmp);
                        Canvas canvas2 = new Canvas(bmp2);
                        // paint defines the text color, stroke width and size
                        Paint color = new Paint();
                        color.setTextSize(35);
                        color.setColor(Color.BLACK);

                        // modify canvas
                        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                                R.drawable.ic_action_whatshot), 0,0, color);


                        // paint defines the text color, stroke width and size

                        // modify canvas
                        canvas2.drawBitmap(BitmapFactory.decodeResource(getResources(),
                                R.drawable.ic_action_flight_land), 0,0, color);

                        // mock
                        MarkerOptions bmpMar1 = new MarkerOptions();
                        bmpMar1.position(latLng3);
                        bmpMar1.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                        // mock
                        MarkerOptions bmpMar2 = new MarkerOptions();
                        bmpMar2.position(latLng4);
                        bmpMar2.icon(BitmapDescriptorFactory.fromBitmap(bmp2));


                        googleMap.addMarker(bmpMar1);
                        googleMap.addMarker(bmpMar2);
                    }
                }, new ErrorHandler());



        activityDisposable = activityObservable
                .map(new ToMostProbableActivity())
                .map(new DetectedActivityToString())
                .subscribe(new DisplayTextOnViewAction(), new ErrorHandler());

        addressDisposable = addressObservable
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String address) throws Exception {
                        mHotspotPresenter.setLastLocation(address);
                        Timber.d("address "+address);
                    }
                }, new ErrorHandler());

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
                                    googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                                        @Override
                                        public void onCircleClick(Circle circle) {
                                            // Flip the red, green and blue components of the circle's stroke color.
                                            circle.setStrokeColor(circle.getStrokeColor() ^ 0x00ffffff);
                                            route( latLngCurrent, circle.getCenter());
                                        }
                                    });

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
        dispose(addressDisposable);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLngCurrent);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        googleMap.moveCamera(center);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        // Start marker
      //  MarkerOptions options = new MarkerOptions();
      //  options.position(start);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_calendar_plus));
      //  googleMap.addMarker(options);

        // End marker
        MarkerOptions options = new MarkerOptions();
        options.position(latLngEnd);
       // options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        googleMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {
        Timber.i("Routing was cancelled.");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private class ErrorHandler implements Consumer<Throwable> {
        @Override
        public void accept(Throwable throwable) {
            Toast.makeText(getApplicationContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Error occurred", throwable);
        }
    }



    public void route(LatLng start,LatLng end)
    {
        latLngEnd = end;
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();
    }

}
