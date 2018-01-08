package org.pl.android.navimee.ui.settings.user.password;


import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

public class UserPasswordChangePresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private final DataManager _dataManager;
    private UserSettingsChangeMvpView _mvpView;

    @Inject
    public UserPasswordChangePresenter(DataManager dataManager) {
        _dataManager = dataManager;
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    public void changePassword(String password) {
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.updatePassword(user, password)
                .subscribe(sub -> _mvpView.onSuccess(), throwable -> _mvpView.onError());
    }
}
