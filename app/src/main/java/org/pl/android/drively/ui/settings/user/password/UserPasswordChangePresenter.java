package org.pl.android.drively.ui.settings.user.password;


import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserPasswordChangePresenter extends BasePresenter<UserPasswordChangeMvpView> {

    private final DataManager dataManager;
    private UserPasswordChangeMvpView userPasswordChangeMvpView;

    @Inject
    public UserPasswordChangePresenter(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void attachView(UserPasswordChangeMvpView mvpView) {
        userPasswordChangeMvpView = mvpView;
    }

    public void changePassword(String password) {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.updatePassword(user, password)
                .subscribe(sub -> userPasswordChangeMvpView.onSuccess(), throwable -> userPasswordChangeMvpView.onError());
    }
}
