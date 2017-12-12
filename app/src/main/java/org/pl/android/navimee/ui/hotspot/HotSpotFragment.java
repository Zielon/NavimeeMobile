package org.pl.android.navimee.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.data.model.FourSquarePlace;
import org.pl.android.navimee.data.model.maps.ClusterItemGoogleMap;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.main.MainActivity;
import org.pl.android.navimee.util.AddressToStringFunc;
import org.pl.android.navimee.util.DetectedActivityToString;
import org.pl.android.navimee.util.DisplayTextOnViewAction;
import org.pl.android.navimee.util.MultiDrawable;
import org.pl.android.navimee.util.ToMostProbableActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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


    @BindView(R.id.mapView)
    MapView mMapView;

    @BindView(R.id.text_place_address)
    TextView mTextPlaceAddress;
    @BindView(R.id.text_place_distance)
    TextView mTextPlaceDistance;
    @BindView(R.id.text_place_time)
    TextView mTextPlaceTime;
    @BindView(R.id.place_driveButton)
    Button mPlaceDriveButton;
    @BindView(R.id.place_closeBotton)
    ImageButton mPlaceCloseButton;
    @BindView(R.id.fab)
    FloatingActionButton filterButton;
    @BindView(R.id.fab_minus)
    FloatingActionButton minusButton;
    @BindView(R.id.fab_plus)
    FloatingActionButton plusButton;
    @BindView(R.id.fab_my_location)
    FloatingActionButton myLocatioButton;


    BottomSheetBehavior mBottomSheetBehavior;

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
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
    GeoFire geoFire;
    private ClusterManager<ClusterItemGoogleMap> mClusterManager;
    private Set<ClusterItemGoogleMap> eventsOnMap = new HashSet<>();
    Timer timer;
    MyFabFragment dialogFrag;
    boolean isFirstAfterPermissionGranted = true;


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
        TextView text = (TextView) ((MainActivity) getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.app_bar_text);
        text.setText(getResources().getString(R.string.hotspot));
        initGeolocation();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hotspot_fragment, container, false);
        ButterKnife.bind(this, rootView);
        mMapView.onCreate(savedInstanceState);
        mHotspotPresenter.attachView(this);


        mMapView.onResume(); // needed to get the map to display immediately

        mBottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.bottomSheetLayout));
        mBottomSheetBehavior.setPeekHeight(300);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        dialogFrag = MyFabFragment.newInstance();
        dialogFrag.setParentFab(filterButton);

        initListeners();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void initListeners() {
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {

                /*if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetHeading.setText(getString(R.string.text_collapse_me));
                } else {
                    bottomSheetHeading.setText(getString(R.string.text_expand_me));
                }*/

                // Check Logs to see how bottom sheets behaves
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
                        break;
                }
            }
            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });
        mPlaceDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(latLngEnd.latitude) + "," +
                        String.valueOf(latLngEnd.longitude));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(mapIntent);
                }
            }
        });

        mPlaceCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(polylines.size()>0) {
                    for (Polyline poly : polylines) {
                        poly.remove();
                    }
                }
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
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
        geoFire = new GeoFire(mHotspotPresenter.getHotSpotDatabaseRefernce());


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
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                        googleMap.moveCamera(yourLocation);
                        //mock
                        LatLng latLng1 = new LatLng(latLng.latitude+0.04,latLng.longitude+0.05);
                        LatLng latLng2 = new LatLng(latLng.latitude-0.04,latLng.longitude-0.05);
                       /* googleMap.addCircle(new CircleOptions()
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
                                .setClickable(true);*/
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

                        mHotspotPresenter.setLastLocationLatLng(latLng);
                        if(isFirstAfterPermissionGranted) {
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                            googleMap.moveCamera(yourLocation);
                            isFirstAfterPermissionGranted = false;
                        }
                        // setup GeoFire
                        latLngCurrent = latLng;
                        eventsOnMap.clear();
                        geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),5).addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                Timber.i(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                                mHotspotPresenter.loadHotSpotPlace(key);
                            }

                            @Override
                            public void onKeyExited(String key) {
                                Timber.i(String.format("Key %s is no longer in the search area", key));
                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {
                                Timber.i(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                            }

                            @Override
                            public void onGeoQueryReady() {
                                Timber.i("All initial data has been loaded and events have been fired!");
                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Timber.e("There was an error with this query: " + error);
                            }
                        });
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
                    .subscribe((Boolean granted) -> {
                        if (granted) { // Always true pre-M
                            onLocationPermissionGranted();
                            mMapView.getMapAsync(new OnMapReadyCallback() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;

                                    // For showing a move to my location button
                                    googleMap.setMyLocationEnabled(true);
                                    googleMap.getUiSettings().setZoomControlsEnabled(false);
                                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                                  /*  googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            route(latLngCurrent,marker.getPosition());
                                            return true;
                                        }
                                    });*/
                                    mClusterManager = new ClusterManager<ClusterItemGoogleMap>(getContext(), googleMap);
                                    mClusterManager.setRenderer(new MapRenderer());
                                    mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterItemGoogleMap>() {
                                        @Override
                                        public boolean onClusterItemClick(ClusterItemGoogleMap clusterItemGoogleMap) {
                                            route(latLngCurrent,clusterItemGoogleMap.getPosition());
                                            return false;
                                        }
                                    });
                                    mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItemGoogleMap>() {
                                        @Override
                                        public boolean onClusterClick(Cluster<ClusterItemGoogleMap> cluster) {
                                            route(latLngCurrent,cluster.getPosition());
                                            return false;
                                        }
                                    });
                                    googleMap.setOnMarkerClickListener(mClusterManager);
                                    googleMap.setOnCameraIdleListener(mClusterManager);
                                    timer = new Timer();

                                    timer.scheduleAtFixedRate(new MyTimerTask(), TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));

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

    @OnClick(R.id.fab)
    public void submit(View view) {
        dialogFrag.show(getActivity().getSupportFragmentManager(), dialogFrag.getTag());
    }
    @OnClick(R.id.fab_my_location)
    public void myLocation(View view) {
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLngCurrent, 14);
        mHotspotPresenter.setLastLocationLatLng(latLngCurrent);
        googleMap.moveCamera(yourLocation);
    }
    @OnClick(R.id.fab_minus)
    public void zoomOut(View view) {
        googleMap.animateCamera(CameraUpdateFactory.zoomOut());
    }
    @OnClick(R.id.fab_plus)
    public void szoomIn(View view) {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        mHotspotPresenter.detachView();
    }


    @Override
    public void onStop() {
        super.onStop();
        dispose(updatableLocationDisposable);
        dispose(lastKnownLocationDisposable);
        dispose(activityDisposable);
        dispose(addressDisposable);
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
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
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLngEnd);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        googleMap.moveCamera(center);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
            mTextPlaceAddress.setText(route.get(i).getEndAddressText());
            mTextPlaceDistance.setText(route.get(i).getDistanceText());
            mTextPlaceTime.setText(route.get(i).getDurationText());
        }

        // Start marker
      //  MarkerOptions options = new MarkerOptions();
      //  options.position(start);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_calendar_plus));
      //  googleMap.addMarker(options);

        // End marker
       // MarkerOptions options = new MarkerOptions();
     //   options.position(latLngEnd);
       // options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
       // googleMap.addMarker(options);
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

    @Override
    public void showEventOnMap(Event event) {
        Timber.d(event.getName());
        if(event.getPlace() != null && event.getPlace().getGeoPoint() != null) {
                ClusterItemGoogleMap clusterItemGoogleMap = new ClusterItemGoogleMap(event.getId(),new LatLng(event.getPlace().getGeoPoint().getLatitude(), event.getPlace().getGeoPoint().getLongitude()),event.getName(),R.drawable.ic_action_whatshot);
                eventsOnMap.add(clusterItemGoogleMap);
            mClusterManager.addItems(eventsOnMap);
        }
      //  googleMap.addMarker(bmpMar);
    }

    @Override
    public void showFoursquareOnMap(FourSquarePlace fourSquarePlace) {
        Timber.d(fourSquarePlace.getName());
        ClusterItemGoogleMap clusterItemGoogleMap = new ClusterItemGoogleMap(fourSquarePlace.getId(),new LatLng(fourSquarePlace.getLocationLat(), fourSquarePlace.getLocationLng()),fourSquarePlace.getName(),R.drawable.ic_action_people);
        eventsOnMap.add(clusterItemGoogleMap);
        mClusterManager.addItems(eventsOnMap);
    }


    protected GoogleMap getMap() {
        return googleMap;
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


    public void clusterMap()
    {
       Timber.i("Clustering");
      //  mClusterManager.clearItems();
        mClusterManager.cluster();
    }




    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clusterMap();
                }
            });
        }
    }





    private class MapRenderer extends DefaultClusterRenderer<ClusterItemGoogleMap> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public MapRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterItemGoogleMap clusterItemGoogleMap, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageResource(clusterItemGoogleMap.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(clusterItemGoogleMap.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ClusterItemGoogleMap> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (ClusterItemGoogleMap p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


}
