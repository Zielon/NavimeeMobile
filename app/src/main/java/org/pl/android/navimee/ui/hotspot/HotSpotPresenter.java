package org.pl.android.navimee.ui.hotspot;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.pl.android.navimee.util.ViewUtil;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-28.
 */

public class HotSpotPresenter extends BasePresenter<HotSpotMvpView> {


    private final DataManager mDataManager;

    private ListenerRegistration mListener;

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
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference("HOTSPOT");
    }

    public String getUid() {
       return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public void loadHotSpotPlace(String key){
        mListener =  mDataManager.getFirebaseService().getFirebaseFirestore().collection("HOTSPOT").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e( "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists() && snapshot.get("hotspotType").equals(Const.HotSpotType.FACEBOOK_EVENT.name())) {
                    Timber.d("Current data: " + snapshot.getData());
                    getMvpView().showOnMap(snapshot.toObject(Event.class));
                }
            }
        });
    }


}
