package org.pl.android.navimee.ui.signup;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class SignUpPresenter extends BasePresenter<SignUpMvpView> {

    private final DataManager mDataManager;
    private SignUpMvpView mMvpView;

    @Inject
    public SignUpPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(SignUpMvpView mvpView) {
        this.mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public void register(String email, String password, String name) {
        RxFirebaseAuth
                .createUserWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(), email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    FirebaseUser user = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    RxFirebaseUser.updateProfile(user, profile).subscribe(sub -> {
                        mMvpView.onSuccess();
                    });
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
            user.put("bigEventsNotification", true);
            user.put("dayScheduleNotification", true);

            mDataManager.getFirebaseService()
                    .getFirebaseFirestore()
                    .collection("USERS").document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> Timber.i("DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Timber.w("Error writing document", e));
        }
    }
}
