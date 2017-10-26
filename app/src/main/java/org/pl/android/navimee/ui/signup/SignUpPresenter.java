package org.pl.android.navimee.ui.signup;



import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.reactivestreams.Subscription;

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

}
