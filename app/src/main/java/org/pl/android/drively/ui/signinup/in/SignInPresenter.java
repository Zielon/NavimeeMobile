package org.pl.android.drively.ui.signinup.in;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.signinup.BaseSignPresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class SignInPresenter extends BaseSignPresenter {

    @Inject
    public SignInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public void loginIn(String email, String password) {
        RxFirebaseAuth.signInWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(), email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError(throwable);
                });
    }

    public void registerMessagingToken() {
        if (mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() != null) {
            String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
            String token = mDataManager.getPreferencesHelper().getValueString(Const.MESSAGING_TOKEN);
            String name = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName();

            Map<String, Object> user = new HashMap<>();

            user.put("token", token);
            user.put("email", mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail());
            user.put("id", userId);
            user.put("name", name);

            mDataManager.getFirebaseService()
                    .getFirebaseFirestore()
                    .collection(FirebasePaths.USERS)
                    .document(userId).set(user, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Timber.i("DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Timber.e("Error writing document", e));

            mDataManager.getFirebaseService()
                    .getFirebaseFirestore()
                    .collection(FirebasePaths.USERS).document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                Map<String, Object> userData = new HashMap<>();

                                if (document.get("bigEventsNotification") == null)
                                    userData.put("bigEventsNotification", true);

                                if (document.get("dayScheduleNotification") == null)
                                    userData.put("dayScheduleNotification", true);

                                if (document.get("avatar") == null)
                                    userData.put("avatar", User.DEFAULT_AVATAR);

                                mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId).update(userData);

                            }
                        }
                    });
        }
    }

}
