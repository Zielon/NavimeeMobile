package org.pl.android.drively.ui.settings.user.name;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

public class UserNameChangePresenter extends BasePresenter<UserNameChangeMvpView> {

    private final DataManager _dataManager;
    private UserNameChangeMvpView _mvpView;

    @Inject
    public UserNameChangePresenter(DataManager dataManager) {
        _dataManager = dataManager;
    }

    @Override
    public void attachView(UserNameChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    public void changeName(String name) {
        FirebaseUser user = _dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        RxFirebaseUser.updateProfile(user, profile)
                .subscribe(sub -> _mvpView.onSuccess(), throwable -> _mvpView.onError());
    }
}
