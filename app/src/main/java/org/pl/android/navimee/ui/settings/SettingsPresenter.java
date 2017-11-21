package org.pl.android.navimee.ui.settings;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.main.MainMvpView;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

/**
 * Created by Wojtek on 2017-11-20.
 */
@ConfigPersistent
public class SettingsPresenter extends BasePresenter<SettingsMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public SettingsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(SettingsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }


    public void logout() {
        mDataManager.getFirebaseService().getFirebaseAuth().signOut();
        getMvpView().onLogout();
    }

}
