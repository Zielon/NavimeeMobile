package org.pl.android.drively.ui.settings.user.name;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserNameChangePresenter extends BasePresenter<UserNameChangeMvpView> {

    private final DataManager dataManager;
    private UserNameChangeMvpView userNameChangeMvpView;

    @Inject
    public UserNameChangePresenter(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public String getName() {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        return user.getDisplayName();
    }

    @Override
    public void attachView(UserNameChangeMvpView mvpView) {
        userNameChangeMvpView = mvpView;
    }

    public void changeName(String name) {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        RxFirebaseUser.updateProfile(user, profile)
                .subscribe(sub -> userNameChangeMvpView.onSuccess(), throwable -> userNameChangeMvpView.onError());
    }
}
