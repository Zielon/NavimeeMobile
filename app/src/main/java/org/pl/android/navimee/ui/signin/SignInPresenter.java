package org.pl.android.navimee.ui.signin;



import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.reactivestreams.Subscription;

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

}
