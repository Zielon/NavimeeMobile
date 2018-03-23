package org.pl.android.drively.ui.signinup;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
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
        FirebaseUser firebaseUser = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (firebaseUser == null) return;

        String userId = firebaseUser.getUid();
        usersRepository.getUser(userId).addOnSuccessListener(user -> mDataManager.getPreferencesHelper().saveUserInfo(user));

        // Mainly for a remote update of a user entity.
        ListenerRegistration registration = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(USERS).document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        mDataManager.getPreferencesHelper().saveUserInfo(user);
                    }
                });

        mDataManager.getPreferencesHelper().setUserListenerRegistration(registration);
    }

    public Task<User> registerUser() {
        FirebaseUser firebaseUser = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        if (firebaseUser == null) return Tasks.forResult(null);

        String userId = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String token = FirebaseInstanceId.getInstance().getToken();

        return usersRepository.getUser(userId).addOnSuccessListener(user -> {
            // A user already registered
            user.setToken(token);
            usersRepository.updateUser(user);
        }).addOnFailureListener(fail -> {
            // A new user
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