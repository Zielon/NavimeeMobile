package org.pl.android.drively.ui.settings.user.email;

import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserEmailChangePresenter extends BasePresenter<UserEmailChangeMvpView> {

    private final DataManager _dataManager;
    private UserEmailChangeMvpView _mvpView;

    @Inject
    public UserEmailChangePresenter(DataManager dataManager) {
        _dataManager = dataManager;
    }

    @Override
    public void attachView(UserEmailChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    public String getEmail() {
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        return user.getEmail();
    }

    public void changeEmail(String newEmail) {
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.updateEmail(user, newEmail)
                .subscribe(sub -> _mvpView.onSuccess(), throwable -> _mvpView.onError(throwable));
    }
}
