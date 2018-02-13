package org.pl.android.drively.ui.hotspot;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.Feedback;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;
import org.pl.android.drively.util.ViewUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class HotSpotPresenter extends BasePresenter<HotSpotMvpView> {


    private final DataManager mDataManager;

    private ListenerRegistration mListener;

    private Set<Const.HotSpotType> filterList = new HashSet<>();
    private Set<String> hotspotKeyList = new HashSet<>();

    @Inject
    public HotSpotPresenter(DataManager dataManager) {
        mDataManager = dataManager;
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

    public void setLastLocation(String location) {
        mDataManager.getPreferencesHelper().setValue(Const.LAST_LOCATION, ViewUtil.deAccent(location.toUpperCase()));
    }

    public void setLastLocationLatLng(LatLng latLng) {
        mDataManager.getPreferencesHelper().setValueFloat(Const.LAST_LOCATION_LAT, (float) latLng.latitude);
        mDataManager.getPreferencesHelper().setValueFloat(Const.LAST_LOCATION_LNG, (float) latLng.longitude);
    }


    public DatabaseReference getHotSpotDatabaseRefernce() {
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.HOTSPOT_CURRENT);
    }

    public String getUid() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public void loadHotSpotPlace(String key) {
        hotspotKeyList.add(key);
        try {
            final String hotspotTypeFilter = nameof(Event.class,"hotspotType");
            mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.HOTSPOT).document(key).addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Timber.e("Listen failed.", e);
                    return;
                }
                hotspotKeyList.remove(key);
                if (snapshot != null && snapshot.exists()) {
                    // Timber.d("Current data: " + snapshot.getData());
                    if (snapshot.get(hotspotTypeFilter).equals(Const.HotSpotType.EVENT.name()) && (filterList.contains(Const.HotSpotType.EVENT) || filterList.isEmpty())) {
                        if (getMvpView() != null) {
                            getMvpView().showEventOnMap(snapshot.toObject(Event.class));
                        }
                    } else if (snapshot.get(hotspotTypeFilter).equals(Const.HotSpotType.FOURSQUARE_PLACE.name()) && (filterList.contains(Const.HotSpotType.FOURSQUARE_PLACE) || filterList.isEmpty())) {
                        if (getMvpView() != null) {
                            getMvpView().showFoursquareOnMap(snapshot.toObject(FourSquarePlace.class));
                        }
                    }

                }

                if (hotspotKeyList.isEmpty()) {
                    if (getMvpView() != null) {
                        getMvpView().clusterMap();
                    }
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
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

    public void addItemToFilterList(Const.HotSpotType item) {
        filterList.add(item);
    }

    public void clearFilterList() {
        filterList.clear();
    }
}
