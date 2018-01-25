package org.pl.android.drively.ui.main;

import com.google.firebase.auth.FirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public MainPresenter(DataManager dataManager) {
        mDataManager = dataManager;
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


    public boolean checkAppIntro() {
        return mDataManager.getPreferencesHelper().getValue("firstStart");
    }

    public FirebaseUser checkLogin() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
    }


    public void setAppIntroShowed() {
        mDataManager.getPreferencesHelper().setValue("firstStart", false);
    }


}
