package org.pl.android.drively.ui.signinup;

import android.app.Activity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.USERS;

public class BaseSignPresenter extends BasePresenter<BaseSignMvpView> {
    protected DataManager mDataManager;
    protected BaseSignMvpView mMvpView;
    @Inject
    UsersRepository usersRepository;

    private FirebaseAnalytics mFirebaseAnalytics;

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
                .subscribe(token -> mMvpView.onSuccess(), throwable -> mMvpView.onError(throwable));
    }

    public void saveUserInfo() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.getUser(userId).addOnSuccessListener(user -> mDataManager.getPreferencesHelper().saveUserInfo(user));

        // Mainly for a remote update of a user entity.
        ListenerRegistration registration = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(USERS).document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    User user = snapshot.toObject(User.class);
                    mDataManager.getPreferencesHelper().saveUserInfo(user);
                });

        mDataManager.getPreferencesHelper().setUserListenerRegistration(registration);
    }

    public Task<User> registerUser() {
        if (mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() == null)
            return Tasks.forResult(null);

        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        String name = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName();
        String email = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail();
        String token = FirebaseInstanceId.getInstance().getToken();

        return usersRepository.getUser(userId).addOnSuccessListener(user -> {
            user.setToken(token);
            usersRepository.updateUser(user);
        }).addOnFailureListener(fail -> {
            User user = new User();
            user.setToken(token);
            user.setEmail(email);
            user.setId(userId);
            user.setName(name);
            usersRepository.updateUser(user);
        }).continueWith(result -> {
            saveUserInfo();
            return result.getResult();
        });
    }
}