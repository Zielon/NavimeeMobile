package org.pl.android.drively.ui.signinup;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.SetOptions;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import timber.log.Timber;

public class BaseSignPresenter extends BasePresenter<BaseSignMvpView> {
    protected DataManager mDataManager;
    protected BaseSignMvpView mMvpView;


    @Override
    public void attachView(BaseSignMvpView mvpView) {
        this.mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public void loginInWithFacebookOrGoogle(AuthCredential credential) {
        RxFirebaseAuth.signInWithCredential(mDataManager.getFirebaseService().getFirebaseAuth(), credential)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError(throwable);
                });
    }

    public void saveUserInfo() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Const.UID = userId;
        mDataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.USERS).document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Timber.e("Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        mDataManager.getPreferencesHelper().saveUserInfo(user);
                    }
                });
    }

    public void registerMessagingToken() {
        if (mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() == null) return;

        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        String token = mDataManager.getPreferencesHelper().getValueString(Const.MESSAGING_TOKEN);
        String name = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName();
        String email = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail();

        mDataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.USERS).document(userId).get()
                .addOnSuccessListener(result -> {
                    User user = !result.exists() ? new User() : result.toObject(User.class);

                    user.setToken(token);
                    user.setEmail(email);
                    user.setId(userId);
                    user.setName(name);

                    mDataManager.getFirebaseService()
                            .getFirebaseFirestore()
                            .collection(FirebasePaths.USERS)
                            .document(userId).set(user, SetOptions.merge());
                });
    }
}