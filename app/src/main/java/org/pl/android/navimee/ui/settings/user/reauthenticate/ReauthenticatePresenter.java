package org.pl.android.navimee.ui.settings.user.reauthenticate;


import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

public class ReauthenticatePresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private final DataManager _dataManager;
    private UserSettingsChangeMvpView _mvpView;

    @Inject
    public ReauthenticatePresenter(DataManager dataManager) {
        _dataManager = dataManager;
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    public void reauthenticate(String email, String password) {
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.reauthenticate(user, EmailAuthProvider.getCredential(email, password))
                .subscribe(sub -> _mvpView.onSuccess(), throwable -> _mvpView.onError());
    }
}
