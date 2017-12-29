package org.pl.android.navimee.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
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
import org.pl.android.navimee.util.Const;
import org.pl.android.navimee.util.DetectedActivityToString;
import org.pl.android.navimee.util.DisplayTextOnViewAction;
import org.pl.android.navimee.util.MultiDrawable;
import org.pl.android.navimee.util.ToMostProbableActivity;
import org.xml.sax.ErrorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        AdapterView.OnItemSelectedListener,RoutingListener,GeoQueryEventListener, AAH_FabulousFragment.Callbacks {


    @BindView(R.id.mapView)
    MapView mMapView;

    @BindView(R.id.text_place_address)
    TextView mTextPlaceAddress;
    @BindView(R.id.text_place_distance)
    TextView mTextPlaceDistance;
    @BindView(R.id.text_place_time)
    TextView mTextPlaceTime;
    @BindView(R.id.text_place_event_name)
    TextView mTextPlaceName;
    @BindView(R.id.text_place_event_count)
    TextView mTextPlaceCount;
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
    String  sEventName,sEventCount;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
    GeoFire geoFire;
    GeoQuery geoQuery;
    private ClusterManager<ClusterItemGoogleMap> mClusterManager;
    private HashMap<String,ClusterItemGoogleMap> eventsOnMap = new HashMap<>();
    MyFabFragment dialogFrag;
    boolean isFirstAfterPermissionGranted = true;
    int durationInSec,distanceValue;
    LatLng locationGeo;
    int radius = 2;

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
        dialogFrag.setCallbacks(HotSpotFragment.this);
      //  setCallbacks((Callbacks) getActivity());
        initListeners();
        if(mHotspotPresenter.getFeedbackBoolean()) {
            mHotspotPresenter.setFeedbackBoolean(false);
            String name = mHotspotPresenter.getFeedbackValue(Const.NAME);
            String address = mHotspotPresenter.getFeedbackValue(Const.LOCATION_ADDRESS);
            String locationName = mHotspotPresenter.getFeedbackValue(Const.LOCATION_NAME);
            String feedbackId = mHotspotPresenter.getFeedbackValue(Const.FEEDBACK_ID);
            showFeedBackDialog(name,address,locationName,feedbackId);
        }

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void showFeedBackDialog(String name, String address, String locationName,String feedBackId) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.feedback)
                .customView(R.layout.dialog_customview, true)
                .build();

         TextView feedBackTextCustom = (TextView) dialog.getCustomView().findViewById(R.id.feedback_custom_text);
         feedBackTextCustom.setText(String.format(getString(R.string.feeback_custom_text), name,locationName));
         TextView feedBackTextNormal = (TextView) dialog.getCustomView().findViewById(R.id.feedback_normal_text);
         feedBackTextNormal.setText(R.string.feeback_normal_text);
         Button yesButton = (Button) dialog.getCustomView().findViewById(R.id.yes_work);
         Button nobutton = (Button) dialog.getCustomView().findViewById(R.id.no_work);
         Button noDrivebutton = (Button) dialog.getCustomView().findViewById(R.id.no_drive);
         yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotspotPresenter.sendFeedbackToServer(feedBackId,0);
                dialog.dismiss();
            }
        });

        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotspotPresenter.sendFeedbackToServer(feedBackId,1);
                dialog.dismiss();
            }
        });

        noDrivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotspotPresenter.sendFeedbackToServer(feedBackId,2);
                dialog.dismiss();
            }
        });

        dialog.show();
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
                mHotspotPresenter.setRouteFromDriver(mTextPlaceAddress.getText().toString(),sEventName,durationInSec,distanceValue,locationGeo);
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
                .setSmallestDisplacement(100)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000 * 2) //Do not receive the updated any frequent than 10 sec
                .setInterval(1000 * 4); // Receive location update every 20 sec

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
        this.geoQuery = this.geoFire.queryAtLocation(new GeoLocation(mHotspotPresenter.getLastLat(), mHotspotPresenter.getLastLng()), radius);


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
                                   Timber.d("ON LOCATION UPDATE");
                                   mHotspotPresenter.setLastLocationLatLng(latLng);
                                   if (isFirstAfterPermissionGranted) {
                                       CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                                       googleMap.moveCamera(yourLocation);
                                       isFirstAfterPermissionGranted = false;
                                   } else {
                                       CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getCameraPosition().zoom);
                                       googleMap.moveCamera(yourLocation);
                                   }
                                   latLngCurrent = latLng;
                                   geoQuery = geoFire.queryAtLocation(new GeoLocation(latLngCurrent.latitude, latLngCurrent.longitude), radius);
                                   geoQuery.addGeoQueryEventListener(HotSpotFragment.this);
                                   eventsOnMap.clear();
                                   mClusterManager.clearItems();

                               }
                    });

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
        this.geoQuery.addGeoQueryEventListener(this);
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
                                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                                  /*  googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            route(latLngCurrent,marker.getPosition());
                                            return true;
                                        }
                                    });*/
                                    if(mClusterManager ==  null) {
                                        mClusterManager = new ClusterManager<ClusterItemGoogleMap>(getContext(), googleMap);
                                        mClusterManager.setRenderer(new MapRenderer());
                                        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterItemGoogleMap>() {
                                            @Override
                                            public boolean onClusterItemClick(ClusterItemGoogleMap clusterItemGoogleMap) {
                                                route(latLngCurrent, clusterItemGoogleMap.getPosition(), clusterItemGoogleMap.getName(), clusterItemGoogleMap.getCount());
                                                return false;
                                            }
                                        });
                                        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItemGoogleMap>() {
                                            @Override
                                            public boolean onClusterClick(Cluster<ClusterItemGoogleMap> cluster) {
                                                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), googleMap.getCameraPosition().zoom + 1);
                                                googleMap.moveCamera(yourLocation);
                                                return false;
                                            }
                                        });
                                    }
                                    googleMap.setOnMarkerClickListener(mClusterManager);
                                    googleMap.setOnCameraIdleListener(mClusterManager);
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
        if(!dialogFrag.isAdded()) {
            dialogFrag.show(getActivity().getSupportFragmentManager(), dialogFrag.getTag());
        }

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
        this.geoQuery.removeAllListeners();
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
            durationInSec = route.get(i).getDurationValue();
            distanceValue = route.get(i).getDistanceValue();
            locationGeo = route.get(i).getLatLgnBounds().getCenter();
            mTextPlaceName.setText(sEventName);
            mTextPlaceCount.setText(sEventCount);
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
        Timber.d(event.getTitle());
        if(!eventsOnMap.containsKey(event.getId()) && event.getPlace() != null && event.getPlace().getGeoPoint() != null) {
            ClusterItemGoogleMap clusterItemGoogleMap = new ClusterItemGoogleMap(event.getId(),new LatLng(event.getPlace().getGeoPoint().getLatitude(), event.getPlace().getGeoPoint().getLongitude()),event.getTitle(),String.valueOf(event.getRank()),event.getHotspotType(),R.drawable.ic_plomien);
            eventsOnMap.put(event.getId(),clusterItemGoogleMap);
            mClusterManager.addItem(clusterItemGoogleMap);
        }
    }

    @Override
    public void showFoursquareOnMap(FourSquarePlace fourSquarePlace) {
        Timber.d(fourSquarePlace.getName());
        if(!eventsOnMap.containsKey(fourSquarePlace.getId())) {
            ClusterItemGoogleMap clusterItemGoogleMap = new ClusterItemGoogleMap(fourSquarePlace.getId(), new LatLng(fourSquarePlace.getLocationLat(), fourSquarePlace.getLocationLng()), fourSquarePlace.getName(), String.valueOf(fourSquarePlace.getStatsVisitsCount()), fourSquarePlace.getHotspotType(), R.drawable.ic_people);
            eventsOnMap.put(fourSquarePlace.getId(), clusterItemGoogleMap);
            mClusterManager.addItem(clusterItemGoogleMap);
        }
    }


    protected GoogleMap getMap() {
        return googleMap;
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Timber.i(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
        mHotspotPresenter.loadHotSpotPlace(key);
    }

    @Override
    public void onKeyExited(String key) {
        Timber.i(String.format("Key %s is no longer in the search area", key));
        if(eventsOnMap.containsKey(key)) {
            mClusterManager.removeItem((ClusterItemGoogleMap) eventsOnMap.get(key));
            eventsOnMap.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Timber.i(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
        if(eventsOnMap.containsKey(key)) {
            Timber.i( "Old Location: " + eventsOnMap.get(key).getPosition());
            Timber.i("New Location: " + location);
            mClusterManager.removeItem(eventsOnMap.get(key));
            eventsOnMap.get(key).setPosition(new LatLng(location.latitude, location.longitude));
            mClusterManager.addItem(eventsOnMap.get(key));
        }
    }

    @Override
    public void onGeoQueryReady() {
        Timber.i("All initial data has been loaded and events have been fired!");
        if(mClusterManager != null) {
            mClusterManager.cluster();
        }
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Timber.e("There was an error with this query: " + error);
    }

    @Override
    public void onResult(Object result) {
        Log.d("k9res", "onResult: " + result.toString());
        Log.d("k9res", "onResult: " + result.toString());
        mHotspotPresenter.clearFilterList();
        if (result.toString().equalsIgnoreCase("swiped_down")) {

        } else {
            if (result != null) {
                ArrayMap<String, List<String>> applied_filters = (ArrayMap<String, List<String>>) result;
                if (applied_filters.size() != 0) {

                    //iterate over arraymap
                    for (Map.Entry<String, List<String>> entry : applied_filters.entrySet()) {
                        Log.d("k9res", "entry.key: " + entry.getKey());
                        if(entry.getValue().contains(getResources().getString(R.string.events_filtr))){
                            mHotspotPresenter.addItemToFilterList(Const.HotSpotType.EVENT);
                        }
                        if(entry.getValue().contains(getResources().getString(R.string.popular_places))) {
                            mHotspotPresenter.addItemToFilterList(Const.HotSpotType.FOURSQUARE_PLACE);
                        }
                        if(entry.getValue().contains(getResources().getString(R.string.uber_ratio))) {
                            mHotspotPresenter.addItemToFilterList(Const.HotSpotType.UBER_MULTIPLIER);
                          /*  for(ClusterItemGoogleMap item : eventsOnMap.values()) {
                                if(item.getType().equals(Const.HotSpotType.UBER_MULTIPLIER)) {

                                }
                            }*/
                        }
                    }
                   // this.geoQuery.setLocation(new GeoLocation(latLngCurrent.latitude, latLngCurrent.longitude), 3);

                } else {

                }
            }
            //handle result
        }
        this.geoQuery = this.geoFire.queryAtLocation(new GeoLocation(latLngCurrent.latitude, latLngCurrent.longitude), radius);
        this.geoQuery.addGeoQueryEventListener(this);
        eventsOnMap.clear();
        mClusterManager.clearItems();

    }


    private class ErrorHandler implements Consumer<Throwable> {
        @Override
        public void accept(Throwable throwable) {
            Toast.makeText(getApplicationContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Error occurred", throwable);
        }
    }



    public void route(LatLng start,LatLng end,String eventName,String eventCount)
    {
        sEventName = eventName;
        sEventCount = eventCount;
        latLngEnd = end;
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();
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
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
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
