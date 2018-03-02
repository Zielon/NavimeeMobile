package org.pl.android.drively.ui.settings.user.email;

import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserEmailChangePresenter extends BasePresenter<UserEmailChangeMvpView> {

    private final DataManager dataManager;
    private UserEmailChangeMvpView userEmailChangeMvpView;

    @Inject
    public UserEmailChangePresenter(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void attachView(UserEmailChangeMvpView mvpView) {
        userEmailChangeMvpView = mvpView;
    }

    public String getEmail() {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        return user.getEmail();
    }

    public void changeEmail(String newEmail) {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.updateEmail(user, newEmail)
                .subscribe(sub -> userEmailChangeMvpView.onSuccess(), throwable -> userEmailChangeMvpView.onError(throwable));
    }
}
