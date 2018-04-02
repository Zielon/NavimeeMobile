package org.pl.android.drively.ui.hotspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.annimon.stream.Stream;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.apache.commons.collections4.ListUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pl.android.drively.BuildConfig;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Car;
import org.pl.android.drively.data.model.CityNotAvailable;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.data.model.eventbus.HotspotSettingsChanged;
import org.pl.android.drively.data.model.eventbus.NotificationEvent;
import org.pl.android.drively.data.model.eventbus.RestartServiceSignal;
import org.pl.android.drively.data.model.maps.ClusterItemGoogleMap;
import org.pl.android.drively.services.GeoLocationUpdateService;
import org.pl.android.drively.services.KalmanFilterService;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.DetectedActivityToString;
import org.pl.android.drively.util.DisplayTextOnViewAction;
import org.pl.android.drively.util.MultiDrawable;
import org.pl.android.drively.util.ToMostProbableActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java8.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

import static com.facebook.FacebookSdk.getApplicationContext;
import static org.pl.android.drively.util.RxUtil.dispose;

public class HotSpotFragment extends BaseTabFragment implements
        HotSpotMvpView,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        AdapterView.OnItemSelectedListener,
        RoutingListener,
        AAH_FabulousFragment.Callbacks {
    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private final static int REQUEST_CHECK_SETTINGS = 0;
    private static KalmanFilterService KALMAN_FILTER = new KalmanFilterService();
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
    @BindView(R.id.place_driveButton)
    FloatingActionButton mPlaceDriveButton;
    @BindView(R.id.place_closeBotton)
    ImageButton mPlaceCloseButton;
    @BindView(R.id.fab)
    FloatingActionButton filterButton;
    @BindView(R.id.fab_filter_remove_badge)
    FancyButton filterRemoveBadge;
    @BindView(R.id.fab_minus)
    FloatingActionButton minusButton;
    @BindView(R.id.fab_plus)
    FloatingActionButton plusButton;
    @BindView(R.id.fab_my_location)
    FloatingActionButton myLocationButton;
    BottomSheetBehavior mBottomSheetBehavior;
    RxPermissions rxPermissions;
    LatLng latLngCurrent, latLngEnd;
    String sEventName, sEventCount;
    Const.HotSpotType sEventType;
    GeoFire geoFireMapPoints, geoFireUsersLocation;
    GeoQuery geoQueryMapPoints, geoQueryUsersLocation;
    MyFabFragment dialogFrag;
    boolean isFirstAfterPermissionGranted = true;
    int durationInSec, distanceValue;
    LatLng locationGeo;
    int radiusForItems = 10;
    int radiusForCars = 5;
    double latNotification;
    double lngNotification;
    boolean isFromNotification = false;
    String notificationName, notificationCount;
    @Inject
    HotSpotPresenter mHotspotPresenter;
    private GoogleMap googleMap;
    private ReactiveLocationProvider locationProvider;
    private Observable<Location> locationUpdatesObservable;
    private Observable<Location> lastKnownLocationObservable;
    private Observable<ActivityRecognitionResult> activityObservable;
    private Observable<Address> addressObservable;
    private Disposable lastKnownLocationDisposable;
    private Disposable updatableLocationDisposable;
    private Disposable activityDisposable;
    private Disposable addressDisposable;
    private List<Polyline> polylineList;
    private ClusterManager<ClusterItemGoogleMap> mClusterManager;
    private ConcurrentHashMap<String, ClusterItemGoogleMap> eventsOnMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Car> usersOnMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, GeoLocation> directionsDelta = new ConcurrentHashMap<>();
    private View rootView;
    private Context context;

    public static HotSpotFragment newInstance() {
        HotSpotFragment fragment = new HotSpotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();

        if (actionBar != null && actionBar.getCustomView() != null) {
            TextView text = (TextView) actionBar.getCustomView().findViewById(R.id.app_bar_text);
            text.setText("");
            actionBar.show();
        }

        if (mHotspotPresenter.checkLogin() != null)
            GeoLocationUpdateService.startService();

        initGeolocation();
        verifyFirstStartSecondHotspotPopup(this.getContext());
        context = this.getContext();
    }

    private void verifyFirstStartSecondHotspotPopup(Context context) {
        if (mHotspotPresenter.getHotspotSecondPopupFirstStart() && !TextUtils.isEmpty(mHotspotPresenter.getDriverType())) {
            HotspotPopupHelper.showSecondPopup(context);
            mHotspotPresenter.setHotspotSecondPopupFirstStart(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.hotspot_fragment, container, false);
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
        restoreDataFromBackup();
        //  setCallbacks((Callbacks) getActivity());
        initListeners();
        if (mHotspotPresenter.getFeedbackBoolean()) {
            mHotspotPresenter.setFeedbackBoolean(false);
            String name = mHotspotPresenter.getFeedbackValue(Const.NAME);
            String address = mHotspotPresenter.getFeedbackValue(Const.LOCATION_ADDRESS);
            String locationName = mHotspotPresenter.getFeedbackValue(Const.LOCATION_NAME);
            String feedbackId = mHotspotPresenter.getFeedbackValue(Const.FEEDBACK_ID);
            showFeedBackDialog(name, address, locationName, feedbackId);
        }

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            latNotification = bundle.getDouble("lat", 0.0);
            lngNotification = bundle.getDouble("lng", 0.0);
            isFromNotification = true;
            notificationName = bundle.getString("name", "");
            notificationCount = bundle.getString("count", "");
        }
        return rootView;
    }

    private void showFeedBackDialog(String name, String address, String locationName, String feedBackId) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.feedback)
                .backgroundColor(getResources().getColor(R.color.primary_dark))
                .contentColor(getResources().getColor(R.color.white))
                .customView(R.layout.dialog_customview, true)
                .build();

        TextView feedBackTextCustom = (TextView) dialog.getCustomView().findViewById(R.id.feedback_custom_text);
        feedBackTextCustom.setText(String.format(getString(R.string.feeback_custom_text), name, locationName));

        TextView feedBackTextNormal = (TextView) dialog.getCustomView().findViewById(R.id.feedback_normal_text);
        feedBackTextNormal.setText(R.string.feeback_normal_text);

        Button yesButton = (Button) dialog.getCustomView().findViewById(R.id.yes_work);
        Button nobutton = (Button) dialog.getCustomView().findViewById(R.id.no_work);
        Button noDrivebutton = (Button) dialog.getCustomView().findViewById(R.id.no_drive);

        yesButton.setOnClickListener(v -> {
            mHotspotPresenter.sendFeedbackToServer(feedBackId, 0);
            dialog.dismiss();
        });

        nobutton.setOnClickListener(v -> {
            mHotspotPresenter.sendFeedbackToServer(feedBackId, 1);
            dialog.dismiss();
        });

        noDrivebutton.setOnClickListener(v -> {
            mHotspotPresenter.sendFeedbackToServer(feedBackId, 2);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void initListeners() {
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                //bottomSheet.animate();
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                //bottomSheet.animate();
            }
        });

        mPlaceDriveButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(latLngEnd.latitude) + "," + String.valueOf(latLngEnd.longitude));
            mHotspotPresenter.setRouteFromDriver(mTextPlaceAddress.getText().toString(), sEventName, durationInSec, distanceValue, locationGeo);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                getContext().startActivity(mapIntent);
            }
        });

        mPlaceCloseButton.setOnClickListener(v -> {
            if (polylineList.size() > 0) {
                for (Polyline poly : polylineList) {
                    poly.remove();
                }
            }
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    @SuppressLint("MissingPermission")
    private void initGeolocation() {
        rxPermissions = new RxPermissions(getActivity());
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        polylineList = new ArrayList<>();
        lastKnownLocationObservable = locationProvider
                .getLastKnownLocation()
                .observeOn(AndroidSchedulers.mainThread());

        final LocationRequest locationRequest = LocationRequest.create()
                .setSmallestDisplacement(20)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000 * 2) //Do not receive the updated any frequent than 2 sec
                .setInterval(1000 * 2); // Receive location update every 2 sec

        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                                .build()
                )
                .doOnNext(locationSettingsResult -> {
                    Status status = locationSettingsResult.getStatus();
                    if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException th) {
                            Log.e("MainActivity", "Error opening settings activity.", th);
                        }
                    }
                })
                .flatMap(locationSettingsResult -> locationProvider.getUpdatedLocation(locationRequest))
                .observeOn(AndroidSchedulers.mainThread());

        activityObservable = locationProvider
                .getDetectedActivity(50)
                .observeOn(AndroidSchedulers.mainThread());

        addressObservable = locationProvider.getUpdatedLocation(locationRequest)
                .flatMap(location -> locationProvider.getReverseGeocodeObservable(Locale.ENGLISH, location.getLatitude(), location.getLongitude(), 1))
                .map(addresses -> addresses != null && !addresses.isEmpty() ? addresses.get(0) : null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        // Init GeoFire queries on the starting position. The listeners are init only once.

        this.geoFireMapPoints = new GeoFire(mHotspotPresenter.getHotSpotDatabaseReference());
        this.geoQueryMapPoints = this.geoFireMapPoints.queryAtLocation(new GeoLocation(mHotspotPresenter.getLastLat(), mHotspotPresenter.getLastLng()), radiusForItems);
        this.geoQueryMapPoints.addGeoQueryDataEventListener(mHotspotPresenter.getMapPointsListener());

        this.geoFireUsersLocation = new GeoFire(mHotspotPresenter.getUsersLocationDatabaseReference());
        this.geoQueryUsersLocation = this.geoFireUsersLocation.queryAtLocation(new GeoLocation(mHotspotPresenter.getLastLat(), mHotspotPresenter.getLastLng()), radiusForCars);
    }

    protected void onLocationPermissionGranted() {
        lastKnownLocationDisposable = lastKnownLocationObservable
                .map(s -> new LatLng(s.getLatitude(), s.getLongitude()))
                .subscribe(latLng -> {
                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                    googleMap.moveCamera(yourLocation);
                }, new ErrorHandler());

        updatableLocationDisposable = locationUpdatesObservable
                .subscribe(location -> {
                    location = KALMAN_FILTER.filter(location);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if(!GeoLocationUpdateService.isServiceRunning(this.getActivity()) && mHotspotPresenter.getShareLocalisationPreference())
                        GeoLocationUpdateService.startService();

                    mHotspotPresenter.setLastLocationLatLng(latLng);
                    if (isFirstAfterPermissionGranted) {
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                        googleMap.animateCamera(yourLocation);
                        isFirstAfterPermissionGranted = false;
                    } else {
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getCameraPosition().zoom);
                        googleMap.animateCamera(yourLocation);
                    }
                    latLngCurrent = latLng;
                    if (isFromNotification) {
                        isFromNotification = false;
                        route(latLng, new LatLng(latNotification, lngNotification), notificationName, notificationCount, Const.HotSpotType.EVENT);
                    }

                    // On each update change the GeoFire position. There is no need to add new listeners.

                    if (geoQueryMapPoints != null)
                        geoQueryMapPoints.setLocation(new GeoLocation(latLngCurrent.latitude, latLngCurrent.longitude), radiusForItems);

                    if (mHotspotPresenter.getShareLocalisationPreference() && geoQueryUsersLocation != null)
                        geoQueryUsersLocation.setLocation(new GeoLocation(latLngCurrent.latitude, latLngCurrent.longitude), radiusForItems);
                });

        activityDisposable = activityObservable
                .map(new ToMostProbableActivity())
                .map(new DetectedActivityToString())
                .subscribe(new DisplayTextOnViewAction(), new ErrorHandler());

        addressDisposable = addressObservable
                .subscribe(address -> {
                    if (!MainActivity.IS_USER_POSITION_CHECKED) {
                        mHotspotPresenter.checkAvailableCities(address.getCountryName(), address.getLocality());
                        MainActivity.IS_USER_POSITION_CHECKED = true;
                    }
                    Timber.d("address " + address);
                }, new ErrorHandler());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mMapView.onResume();

        try {
            this.geoQueryMapPoints.addGeoQueryDataEventListener(mHotspotPresenter.getMapPointsListener());
        } catch (IllegalArgumentException exception) {
            // Added the same listener twice to a GeoQuery!
            exception.printStackTrace();
        }
        determineNecessityAndHandleGeoListeners();
    }

    private void determineNecessityAndHandleGeoListeners() {
        if (!mHotspotPresenter.getShareLocalisationPreference()) {
            Stream.of(usersOnMap).forEach(key -> key.getValue().getMarker().setVisible(false));
            usersOnMap.clear();
            this.geoQueryUsersLocation.removeAllListeners();
        } else {
            try {
                this.geoQueryUsersLocation.addGeoQueryDataEventListener(mHotspotPresenter.getUsersLocationListener());
            } catch (IllegalArgumentException exception) {
                // Added the same listener twice to a GeoQuery!
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backupData();
        onResumeRestore();
        EventBus.getDefault().post(new RestartServiceSignal());
        mMapView.onResume();
    }

    private void onResumeRestore() {
        if (mHotspotPresenter.checkLogin() != null) {
            rxPermissions
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe((Boolean granted) -> {
                        if (granted) { // Always true pre-M
                            onLocationPermissionGranted();
                            mMapView.getMapAsync(mMap -> {
                                googleMap = mMap;
                                // MAP STYLES
                                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));

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
                                if (mClusterManager == null) {
                                    mClusterManager = new ClusterManager<>(getContext(), googleMap);
                                    mClusterManager.setRenderer(new MapRenderer());
                                    mClusterManager.setOnClusterItemClickListener(clusterItemGoogleMap -> {
                                        route(latLngCurrent,
                                                clusterItemGoogleMap.getPosition(),
                                                clusterItemGoogleMap.getName(),
                                                clusterItemGoogleMap.getCount(),
                                                clusterItemGoogleMap.getType());
                                        return false;
                                    });
                                    mClusterManager.setOnClusterClickListener(cluster -> {
                                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), googleMap.getCameraPosition().zoom + 1);
                                        googleMap.animateCamera(yourLocation);
                                        return false;
                                    });
                                }
                                googleMap.setOnMarkerClickListener(mClusterManager);
                                googleMap.setOnCameraIdleListener(mClusterManager);
                            });
                        } else {
                            mMapView.getMapAsync(mMap -> {
                                googleMap = mMap;
                                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));

                                if (!success)
                                    Timber.i("Map style was not loaded");
                            });
                        }
                    });
        }
    }

    private void restoreDataFromBackup() {
        Optional.ofNullable(((MainActivity) context).hotspotFilterBackup).ifPresent(hotspotFilterBackup -> {
            mHotspotPresenter.setCarApplicationFilterList(hotspotFilterBackup.getCarApplicationFilterList());
            mHotspotPresenter.setMapItemFilterList(hotspotFilterBackup.getMapItemFilterList());
            dialogFrag.setApplied_filters(hotspotFilterBackup.getHotspotAppliedFilters());
            updateOnFilterChange(hotspotFilterBackup.getHotspotAppliedFilters());
        });
    }

    @OnClick(R.id.fab)
    public void submit(View view) {
        filterRemoveBadge.setVisibility(View.GONE);
        if (!dialogFrag.isAdded()) {
            dialogFrag.show(getActivity().getSupportFragmentManager(), dialogFrag.getTag());
        }
    }

    @OnClick(R.id.fab_my_location)
    public void myLocation(View view) {
        if (latLngCurrent != null) {
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLngCurrent, 14);
            mHotspotPresenter.setLastLocationLatLng(latLngCurrent);
            googleMap.moveCamera(yourLocation);
        }
    }

    @OnClick(R.id.fab_minus)
    public void zoomOut(View view) {
        googleMap.animateCamera(CameraUpdateFactory.zoomOut());
    }

    @OnClick(R.id.fab_plus)
    public void szoomIn(View view) {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NotificationEvent notificationEvent) {
        route(latLngCurrent,
                new LatLng(notificationEvent.getLat(),
                        notificationEvent.getLng()),
                notificationEvent.getName(),
                notificationEvent.getCount(),
                Const.HotSpotType.EVENT);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        backupData();
    }

    private void backupData() {
        ((MainActivity) context).hotspotFilterBackup = new HotspotFilterBackup(dialogFrag.getApplied_filters(),
                mHotspotPresenter.getMapItemFilterList(),
                mHotspotPresenter.getCarApplicationFilterList());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        mHotspotPresenter.detachView();
        HotSpotFragment.KALMAN_FILTER = new KalmanFilterService();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        dispose(updatableLocationDisposable);
        dispose(lastKnownLocationDisposable);
        dispose(activityDisposable);
        dispose(addressDisposable);

        if (this.geoQueryUsersLocation != null)
            this.geoQueryUsersLocation.removeAllListeners();

        if (this.geoQueryMapPoints != null)
            this.geoQueryMapPoints.removeAllListeners();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLngEnd);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        googleMap.animateCamera(center);

        if (sEventType != null && sEventType.equals(Const.HotSpotType.EVENT))
            rootView.findViewById(R.id.foursquare_icon).setVisibility(View.GONE);
        else
            rootView.findViewById(R.id.foursquare_icon).setVisibility(View.VISIBLE);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (polylineList.size() > 0) {
            for (Polyline poly : polylineList) {
                poly.remove();
            }
        }

        polylineList = new ArrayList<>();

        for (int i = 0; i < route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(R.color.filters_buttons));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylineList.add(polyline);
            mTextPlaceAddress.setText(route.get(i).getEndAddressText());
            mTextPlaceDistance.setText(route.get(i).getDistanceText());
            mTextPlaceTime.setText(route.get(i).getDurationText());
            durationInSec = route.get(i).getDurationValue();
            distanceValue = route.get(i).getDistanceValue();
            locationGeo = route.get(i).getLatLgnBounds().getCenter();
            mTextPlaceName.setText(sEventName);
        }
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
        if (!eventsOnMap.containsKey(event.getId()) && event.getPlace() != null) {
            ClusterItemGoogleMap clusterItemGoogleMap =
                    new ClusterItemGoogleMap(
                            event.getId(),
                            new LatLng(event.getPlace().getLat(),
                                    event.getPlace().getLon()),
                            event.getTitle(),
                            String.valueOf(event.getRank()),
                            event.getHotspotType(),
                            R.drawable.hotspot_24dp);

            clusterItemGoogleMap.setType(Const.HotSpotType.EVENT);
            eventsOnMap.put(event.getId(), clusterItemGoogleMap);
            if (mClusterManager != null)
                mClusterManager.addItem(clusterItemGoogleMap);
        }
    }

    @Override
    public void showFoursquareOnMap(FourSquarePlace fourSquarePlace) {
        if (!eventsOnMap.containsKey(fourSquarePlace.getId())) {
            int picture = R.drawable.people;
            if (fourSquarePlace.getMainCategory().equals("NIGHTLIFE SPOT")) {
                picture = R.drawable.category_nightlife_spot_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("OUTDOORS & RECREATION")) {
                picture = R.drawable.category_outdoor_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("TRAVEL & TRANSPORT")) {
                picture = R.drawable.category_transport_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("ARTS & ENTERTAINMENT")) {
                picture = R.drawable.category_arts_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("PROFESSIONAL & OTHER PLACES")) {
                picture = R.drawable.category_professional_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("COLLEGE & UNIVERSITY")) {
                picture = R.drawable.category_universtiy_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("FOOD")) {
                picture = R.drawable.category_food_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("RESIDENCE")) {
                picture = R.drawable.category_residence_24dp;
            } else if (fourSquarePlace.getMainCategory().equals("SHOP & SERVICE")) {
                picture = R.drawable.category_shop_24dp;
            }

            ClusterItemGoogleMap clusterItemGoogleMap = new ClusterItemGoogleMap(
                    fourSquarePlace.getId(),
                    new LatLng(fourSquarePlace.getLocationLat(), fourSquarePlace.getLocationLng()),
                    fourSquarePlace.getName(),
                    String.valueOf(fourSquarePlace.getStatsVisitsCount()),
                    fourSquarePlace.getHotspotType(),
                    picture);

            eventsOnMap.put(fourSquarePlace.getId(), clusterItemGoogleMap);

            if (mClusterManager != null)
                mClusterManager.addItem(clusterItemGoogleMap);

        } else {
            Optional.ofNullable(mClusterManager).ifPresent(clusterManager -> {
                try {
                    clusterManager.removeItem(eventsOnMap.get(fourSquarePlace.getId()));
                    eventsOnMap.get(fourSquarePlace.getId()).setPosition(new LatLng(fourSquarePlace.getLocationLat(), fourSquarePlace.getLocationLng()));
                    clusterManager.addItem(eventsOnMap.get(fourSquarePlace.getId()));
                } catch (NullPointerException e) {
                    Timber.d(e);
                }
            });
        }
    }

    private Marker addMarker(Car car) {
        GeoLocation location = car.getCurrentLocation();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.latitude, location.longitude)).flat(false);

        if (car.getDriverType().contains(Const.DriverType.UBER.getName())) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.uber));
        } else if (car.getDriverType().contains(Const.DriverType.ITAXI.getName())) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.itaxi));
        } else if (car.getDriverType().contains(Const.DriverType.MY_TAXI.getName())) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mytaxi));
        } else if (car.getDriverType().contains(Const.DriverType.TAXI.getName())) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi2));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.default_car));
        }

        return googleMap.addMarker(markerOptions);
    }

    @Override
    public void showCarOnMap(Car newCar) {
        if (newCar.getUserId() == null || newCar.getUserId().equals(mHotspotPresenter.getUid()))
            return;

        String carUser = newCar.getUserId();
        GeoLocation currentLocation = newCar.getCurrentLocation();

        if (!usersOnMap.containsKey(carUser)) {
            Marker marker = addMarker(newCar);
            marker.setVisible(false);
            newCar.setMarker(marker);
            newCar.setCurrentLocation(currentLocation);
            newCar.setPreviousLocation(currentLocation);
            usersOnMap.put(carUser, newCar);
        } else {
            Car savedCar = usersOnMap.get(carUser);
            GeoLocation previousLocation = savedCar.getCurrentLocation();

            savedCar.setCurrentLocation(currentLocation);
            savedCar.setPreviousLocation(previousLocation);

            if (!previousLocation.equals(currentLocation)) {
                double bearing = savedCar.getBearing();
                Marker marker = savedCar.getMarker();
                mHotspotPresenter.animateMarker(marker,
                        new LatLng(currentLocation.latitude, currentLocation.longitude),
                        bearing, googleMap.getProjection());
            }
        }
    }

    @Override
    public void clusterMap() {
        if (mClusterManager != null) {
            mClusterManager.cluster();
        }
    }

    @Override
    public void showNotAvailableCity(CityNotAvailable city) {
        String title = getResources().getString(R.string.not_available_in_city, city.getCity());
        city.setCity(city.getCity().toUpperCase());
        MaterialDialog dialogAlert = new MaterialDialog.Builder(getActivity())
                .title(title)
                .backgroundColor(getResources().getColor(R.color.primary_dark))
                .contentColor(getResources().getColor(R.color.white))
                .positiveText(R.string.let_us_know)
                .positiveColor(getResources().getColor(R.color.white))
                .negativeText(R.string.cancel)
                .onPositive((MaterialDialog dialog, DialogAction which) -> mHotspotPresenter.sendMessageWhenCityNotAvailable(city))
                .build();

        dialogAlert.show();
    }

    protected GoogleMap getMap() {
        return googleMap;
    }

    @Override
    public void removeCarFromMap(Car car) {
        String carUser = car.getUserId();
        if (carUser == null) return;
        directionsDelta.remove(carUser);
        if (usersOnMap.containsKey(carUser)) {
            usersOnMap.get(carUser).getMarker().remove();
            usersOnMap.remove(carUser);
        }
    }

    @Override
    public void removeItemFromMap(String id) {
        if (id == null) return;
        if (eventsOnMap.containsKey(id)) {
            mClusterManager.removeItem(eventsOnMap.get(id));
            eventsOnMap.remove(id);
        }
    }

    @Override
    public void onResult(Object result) {
        updateOnFilterChange(result);
    }

    private void updateOnFilterChange(Object result) {

        mHotspotPresenter.clearFilters();
        filterRemoveBadge.setVisibility(View.GONE);

        final List<String> carsFiltersTypes = new ArrayList<>();
        final List<String> itemsFiltersTypes = new ArrayList<>();

        if (!result.toString().equalsIgnoreCase("swiped_down")) {
            ArrayMap<String, List<String>> applied_filters = (ArrayMap<String, List<String>>) result;
            Optional.ofNullable(((MainActivity) context).hotspotFilterBackup)
                    .ifPresent(hotspotFilterBackup -> hotspotFilterBackup.setHotspotAppliedFilters(applied_filters));
            if (applied_filters.size() > 0) {
                filterRemoveBadge.setVisibility(View.VISIBLE);
                filterRemoveBadge.setText(String.valueOf(applied_filters.get(getString(R.string.visible_on_map)).size()));
                for (Map.Entry<String, List<String>> entry : applied_filters.entrySet()) {
                    if (entry.getValue().contains(getResources().getString(R.string.events_filtr))) {
                        mHotspotPresenter.addItemToMapItemFilterList(Const.HotSpotType.EVENT.name());
                        itemsFiltersTypes.add(Const.HotSpotType.FOURSQUARE_PLACE.name());
                    }
                    if (entry.getValue().contains(getResources().getString(R.string.popular_places))) {
                        mHotspotPresenter.addItemToMapItemFilterList(Const.HotSpotType.FOURSQUARE_PLACE.name());
                        itemsFiltersTypes.add(Const.HotSpotType.FOURSQUARE_PLACE.name());
                    }

                    // Cars filter types
                    List<String> filters = entry.getValue();
                    List<String> applications = Stream.of(new ArrayList<>(Arrays.asList(Const.DriverType.values()))).map(Const.DriverType::getName).toList();
                    carsFiltersTypes.addAll(ListUtils.intersection(applications, filters));
                    Stream.of(carsFiltersTypes).forEach(app -> mHotspotPresenter.addCarApplicationFilterList(app));
                }
            }
        }

        // CARS
        Stream.of(usersOnMap).forEach(key -> key.getValue().getMarker().setVisible(true));
        Stream.of(usersOnMap)
                .filter(car -> carsFiltersTypes.contains(car.getValue().getDriverType()))
                .forEach(key -> key.getValue().getMarker().setVisible(false));

        // CLUSTERS
        Optional.ofNullable(mClusterManager).ifPresent(clusterManager -> {
            mClusterManager.clearItems();
            determineEventFilterNecessity(itemsFiltersTypes, Const.HotSpotType.FOURSQUARE_PLACE);
            determineEventFilterNecessity(itemsFiltersTypes, Const.HotSpotType.EVENT);
            // Try to re-render the map?
            mClusterManager.setRenderer(new MapRenderer());
        });
    }

    private void determineEventFilterNecessity(List<String> itemsFiltersTypes, Const.HotSpotType hotSpotType) {
        Stream<Map.Entry<String, ClusterItemGoogleMap>> filteredStream = Stream.of(eventsOnMap)
                .filter(item -> item.getValue().getType().equals(hotSpotType));
        if (!itemsFiltersTypes.contains(hotSpotType.name()))
            filteredStream.forEach(item -> mClusterManager.addItem(item.getValue()));
    }

    public void route(LatLng start, LatLng end, String eventName, String eventCount, Const.HotSpotType hotSpotType) {
        sEventName = eventName;
        sEventCount = eventCount;
        sEventType = hotSpotType;
        latLngEnd = end;

        if (BuildConfig.DEBUG) {
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, end)
                    .build();
            routing.execute();
        } else {
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, end)
                    .key(BuildConfig.GOOGLE_DIRECTIONS_KEY)
                    .build();
            routing.execute();
        }
    }

    @Override
    public void showInstructionPopup() {
        if (mHotspotPresenter.getHotspotFirstPopupFirstStart() && TextUtils.isEmpty(mHotspotPresenter.getDriverType())
                && !mHotspotPresenter.getShareLocalisationPreference()) {
            HotspotPopupHelper.showFirstPopup(context, mHotspotPresenter.getDriverType(),
                    selectedDriverType -> {
                        mHotspotPresenter.updateShareLocalisation(true);
                        mHotspotPresenter.saveDriverType(selectedDriverType.getName());
                        EventBus.getDefault().post(new HotspotSettingsChanged(selectedDriverType.getName(), true));
                        HotspotPopupHelper.showSecondPopup(context);
                        mHotspotPresenter.setHotspotSecondPopupFirstStart(false);
                        determineNecessityAndHandleGeoListeners();
                        restoreDataFromBackup();
                        onResumeRestore();
                    }, () -> Timber.d("Dismissed"));
        } else {
            HotspotPopupHelper.showSecondPopup(context);
        }
    }

    private class ErrorHandler implements Consumer<Throwable> {
        @Override
        public void accept(Throwable throwable) {
            Log.d("MainActivity", "Error occurred", throwable);
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
            mImageView.setImageResource(clusterItemGoogleMap.getProfilePhoto());
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ClusterItemGoogleMap> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            if (isAdded()) {

                List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
                int width = mDimension;
                int height = mDimension;

                for (ClusterItemGoogleMap p : cluster.getItems()) {
                    // Draw 4 at most.
                    if (profilePhotos.size() == 4) break;
                    Drawable drawable = getResources().getDrawable(p.getProfilePhoto());
                    drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                }
                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                multiDrawable.setBounds(0, 0, width, height);

                mClusterImageView.setImageDrawable(multiDrawable);
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Data
    @AllArgsConstructor
    public class HotspotFilterBackup {
        private ArrayMap<String, List<String>> hotspotAppliedFilters = new ArrayMap<>();
        private Set<String> mapItemFilterList = new HashSet<>();
        private Set<String> carApplicationFilterList = new HashSet<>();
    }
}
