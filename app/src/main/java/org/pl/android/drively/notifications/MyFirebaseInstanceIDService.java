package org.pl.android.drively.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

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
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        if (dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() != null) {

            String userId = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
            String name = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName();
            String email = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail();

            User user = new User();
            user.setToken(token);
            user.setEmail(email);
            user.setId(userId);
            user.setName(name);

            usersRepository.updateUser(user);
        }
    }
}
