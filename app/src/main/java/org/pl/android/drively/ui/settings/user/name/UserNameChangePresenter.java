package org.pl.android.drively.ui.settings.user.name;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

public class UserNameChangePresenter extends BasePresenter<UserNameChangeMvpView> {

    private final DataManager dataManager;
    private final UsersRepository usersRepository;
    private UserNameChangeMvpView userNameChangeMvpView;

    @Inject
    public UserNameChangePresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.dataManager = dataManager;
        this.usersRepository = usersRepository;
    }

    public String getName() {
        FirebaseUser user = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if(user == null) return "An empty name!";
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
                .subscribe(sub -> {
                    try {
                        usersRepository.updateUserField(user.getUid(), "name", name);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    userNameChangeMvpView.onSuccess();
                }, throwable -> userNameChangeMvpView.onError());
    }
}
