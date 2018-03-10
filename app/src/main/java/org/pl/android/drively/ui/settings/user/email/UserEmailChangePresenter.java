package org.pl.android.drively.ui.settings.user.email;

import com.google.firebase.auth.FirebaseUser;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserEmailChangePresenter extends BasePresenter<UserEmailChangeMvpView> {

    private final DataManager dataManager;
    private final UsersRepository usersRepository;
    private UserEmailChangeMvpView userEmailChangeMvpView;

    @Inject
    public UserEmailChangePresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.dataManager = dataManager;
        this.usersRepository = usersRepository;
    }

    @Override
    public void attachView(UserEmailChangeMvpView mvpView) {
        userEmailChangeMvpView = mvpView;
    }

    public String getEmail() {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return "An empty email!";
        return user.getEmail();
    }

    public void changeEmail(String email) {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (user == null) return;
        RxFirebaseUser.updateEmail(user, email)
                .subscribe(sub -> {
                    try {
                        usersRepository.updateUserField(user.getUid(), "email", email);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    userEmailChangeMvpView.onSuccess();
                }, throwable -> userEmailChangeMvpView.onError(throwable));
    }
}
