package org.pl.android.navimee.ui.hotspot;

import com.google.firebase.auth.FirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;

import javax.inject.Inject;

/**
 * Created by Wojtek on 2017-10-28.
 */

public class HotSpotPresenter extends BasePresenter<HotSpotMvpView> {


    private final DataManager mDataManager;

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
        mDataManager.getPreferencesHelper().setValue(Const.LAST_LOCATION,location.toUpperCase());
    }


}
