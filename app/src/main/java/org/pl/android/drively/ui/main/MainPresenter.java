package org.pl.android.drively.ui.main;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.firebase.auth.FirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.remote.FirebaseAnalyticsService;
import org.pl.android.drively.services.GeoLocationUpdateService;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    private Disposable mDisposable;

    @Inject
    public MainPresenter(DataManager dataManager, UsersRepository usersRepository,
                         FirebaseAnalyticsService firebaseAnalyticsService) {
        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
        this.firebaseAnalyticsService = firebaseAnalyticsService;
    }

    @Override
    public void attachView(MainMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void updateOnlineStatus(boolean online) {
        try {
            String userId = mDataManager.getPreferencesHelper().getUserId();
            if (userId.equals("")) return;
            usersRepository.updateUserField(userId, "online", online);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public boolean checkAppIntro() {
        return mDataManager.getPreferencesHelper().getValue(Const.FIRST_START);
    }

    public boolean isLogin() {
        FirebaseUser user = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) {
            if (GeoLocationUpdateService.FIREBASE_KEY != null && !GeoLocationUpdateService.FIREBASE_KEY.isEmpty())
                mDataManager.getFirebaseService()
                        .getFirebaseDatabase()
                        .getReference(FirebasePaths.USER_LOCATION)
                        .child(GeoLocationUpdateService.FIREBASE_KEY)
                        .removeValue();
            return false;
        }
        return true;
    }

    public void checkVersion() {
        try {
            PackageInfo packageInfo = ((Context) getMvpView()).getPackageManager().getPackageInfo(((Context) getMvpView()).getPackageName(), 0);
            int currentVersion = packageInfo.versionCode;
            int oldVersion = mDataManager.getPreferencesHelper().getAppVersion();
            if (oldVersion < currentVersion) {
                mDataManager.getFirebaseService().getFirebaseAuth().signOut();
                mDataManager.getPreferencesHelper().clear();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setAppIntroShowed() {
        mDataManager.getPreferencesHelper().setValue(Const.FIRST_START, false);
    }

    public String getUserId() {
        return mDataManager.getPreferencesHelper().getUserId();
    }

    public boolean updateUserLocation() {
        return mDataManager.getPreferencesHelper().getUserInfo().isShareLocalization();
    }

    public void logAnalytics(String id, String name, Object content) {
        firebaseAnalyticsService.reportEvent(id, name, content);
    }
}
