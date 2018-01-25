package org.pl.android.drively.ui.signup;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

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
                    RxFirebaseUser.updateProfile(user, profile).subscribe(sub -> mMvpView.onSuccess());
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

            User user = new User();

            user.setToken(token);
            user.setEmail(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail());
            user.setId(userId);
            user.setName(name);

            mDataManager.getFirebaseService()
                    .getFirebaseFirestore()
                    .collection(FirebasePaths.USERS).document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> Timber.i("DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Timber.w("Error writing document", e));
        }
    }
}
