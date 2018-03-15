package org.pl.android.drively.notifications;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Inject
    DataManager dataManager;

    @Inject
    UsersRepository usersRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        dataManager.getPreferencesHelper().setValue(Const.MESSAGING_TOKEN, refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        if (dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() == null)
            return;

        FirebaseUser firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();

        String userId = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();

        User user = new User();

        user.setToken(token);
        user.setEmail(email);
        user.setId(userId);
        user.setName(name);

        usersRepository.updateUser(user);
    }
}
