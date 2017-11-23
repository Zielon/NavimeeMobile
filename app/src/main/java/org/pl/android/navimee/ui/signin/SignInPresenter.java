package org.pl.android.navimee.ui.signin;



import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;


public class SignInPresenter extends BasePresenter<SignInMvpView> {

    private final DataManager mDataManager;
    private SignInMvpView mMvpView;
    private Subscription mSubscription;

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
        mDataManager.getPreferencesHelper().setValue("notlogged",false);
    }

    public void loginIn(String email, String password) {
        //  mDataManager.getFirebaseService().signUp(email, password);
        RxFirebaseAuth.signInWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(),email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError();
                });
    }


    public void loginInWithFacebook(AuthCredential credential) {
        RxFirebaseAuth.signInWithCredential(mDataManager.getFirebaseService().getFirebaseAuth(),credential)
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
            user.put("name", mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName());
            mDataManager.getFirebaseService().getFirebaseFirestore().collection("users").document(userId).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Timber.i("DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @SuppressLint("TimberArgCount")
                @Override
                public void onFailure(@NonNull Exception e) {
                    Timber.e("Error writing document", e);
                }
            });
        }
    }

}
