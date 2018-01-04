package org.pl.android.navimee.ui.settings.user;

import com.google.firebase.auth.FirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserSettingsPresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private FirebaseUser firebaseUser;

    @Inject
    public UserSettingsPresenter(DataManager dataManager) {
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }

    public String getName() {
        return firebaseUser.getDisplayName();
    }
}
