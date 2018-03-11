package org.pl.android.drively.ui.hotspot;

import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.annimon.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.pl.android.drively.contracts.repositories.CoordinatesRepository;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Car;
import org.pl.android.drively.data.model.CityNotAvailable;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.Feedback;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.ui.base.tab.BaseTabPresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;
import org.pl.android.drively.util.ViewUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

public class HotSpotPresenter extends BaseTabPresenter<HotSpotMvpView> {

    private final UsersRepository usersRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    private Set<String> mapItemFilterList = new HashSet<>();
    private Set<String> carApplicationFilterList = new HashSet<>();

    @Inject
    public HotSpotPresenter(
            DataManager dataManager,
            UsersRepository usersRepository,
            CoordinatesRepository coordinatesRepository) {

        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
        this.coordinatesRepository = coordinatesRepository;
    }

    public FirebaseUser checkLogin() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
    }

    @Override
    public void attachView(HotSpotMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    private void setLastLocation(String location) {
        mDataManager.getPreferencesHelper().setValue(Const.LAST_LOCATION, ViewUtil.deAccent(location.toUpperCase()));
    }

    public void setLastLocationLatLng(LatLng latLng) {
        mDataManager.getPreferencesHelper().setValueFloat(Const.LAST_LOCATION_LAT, (float) latLng.latitude);
        mDataManager.getPreferencesHelper().setValueFloat(Const.LAST_LOCATION_LNG, (float) latLng.longitude);
    }

    public DatabaseReference getHotSpotDatabaseReference() {
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.HOTSPOT_CURRENT);
    }

    public DatabaseReference getUsersLocationDatabaseReference() {
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION);
    }

    private void updateCarView(DataSnapshot dataSnapshot, GeoLocation location) {
        if (dataSnapshot == null || !dataSnapshot.exists()) return;
        String[] parts = dataSnapshot.getKey().split("_");
        if (parts.length < 2) return;
        Car car = new Car();
        car.setDriverType(parts[0]);
        car.setUserId(parts[1]);
        car.setGeoLocation(location);
        if (carApplicationFilterList.contains(car.getDriverType())) return;
        if (getMvpView() != null) {
            getMvpView().showCarOnMap(car);
        }
    }

    public GeoQueryDataEventListener getUsersLocationListener() {
        UsersLocationListener usersLocationListener = new UsersLocationListener();
        usersLocationListener.setName("UsersLocationListener");
        return usersLocationListener;
    }

    private void updateMapItem(DataSnapshot dataSnapshot, GeoLocation location) {
        if (dataSnapshot == null || !dataSnapshot.exists()) return;

        HashMap<String, Object> data = ((HashMap<String, Object>) dataSnapshot.getValue());

        if (!data.containsKey("hotspotType")) return;

        String type = data.get("hotspotType").toString();

        switch (Const.HotSpotType.valueOf(type)) {
            case EVENT:
                Event event = mapper.convertValue(dataSnapshot.getValue(), Event.class);
                if (mapItemFilterList.contains(Const.HotSpotType.EVENT.name())) return;
                if (getMvpView() != null)
                    getMvpView().showEventOnMap(event);
                break;

            case FOURSQUARE_PLACE:
                FourSquarePlace fourSquarePlace = mapper.convertValue(dataSnapshot.getValue(), FourSquarePlace.class);
                if (mapItemFilterList.contains(Const.HotSpotType.FOURSQUARE_PLACE.name())) return;
                if (getMvpView() != null)
                    getMvpView().showFoursquareOnMap(fourSquarePlace);
                break;
        }
    }

    public GeoQueryDataEventListener getMapPointsListener() {
        MapPointsListener mapPointsListener = new MapPointsListener();
        mapPointsListener.setName("MapPointsListener");
        return mapPointsListener;
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final double bearing, final boolean hideMarker, Projection projection) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Point startPoint = projection.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation((float) bearing);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public double calculateBearing(double startLat, double startLong, double endLat, double endLong) {
        double dLon = (endLong - startLong);
        double y = Math.sin(dLon) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLon);
        double bearing = Math.toDegrees((Math.atan2(y, x)));
        return (360 - ((bearing + 360) % 360));
    }

    public String getUid() {
        return mDataManager.getPreferencesHelper().getUserId();
    }

    public void setRouteFromDriver(String locationAddress, String locationName, int durationInSec, int distanceValue, LatLng latLng) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setLocationAddress(locationAddress);
        feedback.setLocationName(locationName);
        feedback.setDurationInSec(durationInSec);
        feedback.setDistanceValue(distanceValue);
        feedback.setGeoPoint(latLng);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        String formattedDate = df.format(c);
        feedback.setDate(formattedDate);
        mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child(FirebasePaths.FEEDBACK).push().setValue(feedback);
    }

    public double getLastLat() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LAT);
    }

    public double getLastLng() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LNG);
    }

    public boolean getFeedbackBoolean() {
        return mDataManager.getPreferencesHelper().getValueWithDefaultFalse(Const.IS_FEEDBACK);
    }

    public void setFeedbackBoolean(boolean var) {
        mDataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK, var);
    }

    public String getFeedbackValue(String name) {
        return mDataManager.getPreferencesHelper().getValueString(name);
    }

    public void sendFeedbackToServer(String feedbackId, int feedbackAnswer) {
        Map<String, Object> feedbackAnswerMap = new HashMap<>();
        feedbackAnswerMap.put("feedbackAnswer", feedbackAnswer);
        mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child(FirebasePaths.FEEDBACK).child(feedbackId).updateChildren(feedbackAnswerMap);
    }

    public void sendMessageWhenCityNotAvailable(CityNotAvailable city) {
        coordinatesRepository.updateUnavailableCity(city);
    }

    public void checkAvailableCities(String countryName, String locality) {
        try {
            setLastLocation(locality);
            usersRepository.updateUserField(mDataManager.getPreferencesHelper().getUserId(), "country", countryName.toUpperCase());
            usersRepository.updateUserField(mDataManager.getPreferencesHelper().getUserId(), "city", locality.toUpperCase());
            coordinatesRepository.getAvailableCities(countryName).addOnSuccessListener(cities -> {
                if (Stream.of(cities).allMatch(city -> !city.getName().toUpperCase().equals(locality.toUpperCase()))) {
                    CityNotAvailable cityNotAvailable = new CityNotAvailable();
                    cityNotAvailable.setCity(locality.toUpperCase());
                    cityNotAvailable.setCountryName(countryName.toUpperCase());
                    if (getMvpView() != null)
                        getMvpView().showNotAvailableCity(cityNotAvailable);
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void saveDriverType(String name) {
        mDataManager.getPreferencesHelper().setValue(Const.DRIVER_TYPE, name);
        try {
            usersRepository.updateUserField(mDataManager.getPreferencesHelper().getUserId(), "driverType", name);
        } catch (NoSuchFieldException e) {
            Timber.d(e);
        }
    }

    public String getDriverType() {
        return mDataManager.getPreferencesHelper().getValueString(Const.DRIVER_TYPE);
    }

    public boolean getShareLocalisationPreference() {
        return mDataManager.getPreferencesHelper().getValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION);
    }

    public void updateShareLocalisation(boolean shareLocalisation) {
        mDataManager.getPreferencesHelper().setValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, true);
        try {
            usersRepository.updateUserField(mDataManager.getPreferencesHelper().getUserId(), Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalisation);
        } catch (NoSuchFieldException e) {
            Timber.d(e);
        }
    }

    public boolean getHotspotSecondPopupFirstStart() {
        return mDataManager.getPreferencesHelper().getValue(Const.HOTSPOT_SECOND_POPUP_FIRST_START);
    }

    public void setHotspotSecondPopupFirstStart(boolean value) {
        mDataManager.getPreferencesHelper().setValue(Const.HOTSPOT_SECOND_POPUP_FIRST_START, value);
    }

    public boolean getHotspotFirstPopupFirstStart() {
        return mDataManager.getPreferencesHelper().getValue(getMvpView().getClass().getSimpleName() + Const.FIRST_START_POPUP_SUFFIX);
    }

    public void addItemToMapItemFilterList(String item) {
        mapItemFilterList.add(item);
    }

    public void addCarApplicationFilterList(String item) {
        carApplicationFilterList.add(item);
    }

    public void clearFilters() {
        mapItemFilterList.clear();
        carApplicationFilterList.clear();
    }

    public class UsersLocationListener extends BaseGeoFireListener {
        @Override
        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
            updateCarView(dataSnapshot, location);
        }

        @Override
        public void onDataExited(DataSnapshot dataSnapshot) {
            String[] parts = dataSnapshot.getKey().split("_");
            if (parts.length < 2) return;
            Car car = new Car();
            car.setDriverType(parts[0]);
            car.setUserId(parts[1]);
            if (getMvpView() != null) {
                getMvpView().removeCarFromMap(car);
            }
        }

        @Override
        public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
            updateCarView(dataSnapshot, location);
        }

        @Override
        public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
            updateCarView(dataSnapshot, location);
        }

        @Override
        public void onGeoQueryReady() {
            if (getMvpView() != null) {
                getMvpView().clusterMap();
            }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    }

    public class MapPointsListener extends BaseGeoFireListener {
        @Override
        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
            updateMapItem(dataSnapshot, location);
        }

        @Override
        public void onDataExited(DataSnapshot dataSnapshot) {
            if (getMvpView() != null)
                getMvpView().removeItemFromMap(dataSnapshot.getKey());
        }

        @Override
        public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
        }

        @Override
        public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
        }

        @Override
        public void onGeoQueryReady() {
            if (getMvpView() != null) {
                getMvpView().clusterMap();
            }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    }
}
