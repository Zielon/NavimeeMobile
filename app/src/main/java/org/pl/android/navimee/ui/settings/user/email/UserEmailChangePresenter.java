package org.pl.android.navimee.ui.settings.user.email;

import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

public class UserEmailChangePresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private final DataManager _dataManager;
    private UserSettingsChangeMvpView _mvpView;

    @Inject
    public UserEmailChangePresenter(DataManager dataManager) {
        _dataManager = dataManager;
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    public void changeEmail(String newEmail){
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if(user == null) return;
        RxFirebaseUser.updateEmail(user, newEmail)
                .subscribe(sub -> _mvpView.onSuccess(), throwable -> _mvpView.onError());
    }
}
