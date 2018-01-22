package org.pl.android.drively.ui.signin;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.ChatUser;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class SignInPresenter extends BasePresenter<SignInMvpView> {

    private final DataManager mDataManager;
    private SignInMvpView mMvpView;

    @Inject
    public SignInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(SignInMvpView mvpView) {
        this.mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public void setLogin() {
        mDataManager.getPreferencesHelper().setValue("notlogged", false);
    }

    public void loginIn(String email, String password) {
        RxFirebaseAuth.signInWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(), email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError();
                });
    }

    public void loginInWithFacebookOrGoogle(AuthCredential credential) {
        RxFirebaseAuth.signInWithCredential(mDataManager.getFirebaseService().getFirebaseAuth(), credential)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError();
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

                                if(document.get("dayScheduleNotification") == null)
                                    userData.put("dayScheduleNotification", true);

                                if(document.get("avatar") == null)
                                    userData.put("avatar", User.DEFAULT_AVATAR);

                                mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId).update(userData);

                            }
                        }
                    });
        }
    }

    public void saveUserInfo() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Const.UID = userId;
        mDataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.USERS).document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Timber.e( "Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Timber.d("Current data: " + snapshot.getData());
                       ChatUser chatUser =  documentSnapshot.toObject(ChatUser.class);
                       mDataManager.getPreferencesHelper().saveUserInfo(chatUser);
                    }
                });
    }
}
