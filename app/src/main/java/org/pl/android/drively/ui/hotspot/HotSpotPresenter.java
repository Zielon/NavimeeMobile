package org.pl.android.drively.ui.hotspot;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.ViewUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-28.
 */

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
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference("HOTSPOT_CURRENT");
    }

    public String getUid() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public void loadHotSpotPlace(String key) {
        hotspotKeyList.add(key);
        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("HOTSPOT").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e("Listen failed.", e);
                    return;
                }
                hotspotKeyList.remove(key);
                if (snapshot != null && snapshot.exists()) {
                    // Timber.d("Current data: " + snapshot.getData());
                    if (snapshot.get("hotspotType").equals(Const.HotSpotType.EVENT.name()) && (filterList.contains(Const.HotSpotType.EVENT) || filterList.isEmpty())) {
                        if (getMvpView() != null) {
                            getMvpView().showEventOnMap(snapshot.toObject(Event.class));
                        }
                    } else if (snapshot.get("hotspotType").equals(Const.HotSpotType.FOURSQUARE_PLACE.name()) && (filterList.contains(Const.HotSpotType.FOURSQUARE_PLACE) || filterList.isEmpty())) {
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
            }
        });
    }

    public void setRouteFromDriver(String locationAddress, String locationName, int durationInSec, int distanceValue, LatLng latLng) {
        HashMap<String, Object> feedBackObject = new HashMap<>();
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        feedBackObject.put("userId", userId);
        feedBackObject.put("locationAddress", locationAddress);
        feedBackObject.put("locationName", locationName);
        feedBackObject.put("durationInSec", durationInSec);
        feedBackObject.put("distance", distanceValue);
        feedBackObject.put("geoPoint", latLng);
        mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("FEEDBACK").push().setValue(feedBackObject);
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
        mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("FEEDBACK").child(feedbackId).updateChildren(feedbackAnswerMap);
    }

    public void addItemToFilterList(Const.HotSpotType item) {
        filterList.add(item);
    }

    public void clearFilterList() {
        filterList.clear();
    }


}
