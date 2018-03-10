package org.pl.android.drively.ui.main;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.firebase.auth.FirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private Disposable mDisposable;

    @Inject
    public MainPresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
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

    public FirebaseUser checkLogin() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
    }

    public void checkVersion() {
        try {
            PackageInfo packageInfo = ((Context) getMvpView()).getPackageManager().getPackageInfo(((Context) getMvpView()).getPackageName(), 0);
            int currentVersion = packageInfo.versionCode;
            int oldVersion = mDataManager.getPreferencesHelper().getAppVersion();
            if (oldVersion < currentVersion && oldVersion != -1) {
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
}
