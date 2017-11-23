package org.pl.android.navimee.ui.signup;



import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;


public class SignUpPresenter extends BasePresenter<SignUpMvpView> {

    private final DataManager mDataManager;
    private SignUpMvpView mMvpView;
    private Subscription mSubscription;

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



    public void register(String email, String password) {
      //  mDataManager.getFirebaseService().signUp(email, password);
        RxFirebaseAuth.createUserWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(),email, password)
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
        if(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() != null) {
            String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
            String token = mDataManager.getPreferencesHelper().getValueString(Const.MESSAGING_TOKEN);
            Map<String, Object> user = new HashMap<>();
            user.put("token", token);
            user.put("email", mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail());
            mDataManager.getFirebaseService().getFirebaseFirestore().collection("users").document(userId).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Timber.i("DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Timber.w("Error writing document", e);
                }
            });
        }
    }
}
