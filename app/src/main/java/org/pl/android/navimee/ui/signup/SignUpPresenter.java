package org.pl.android.navimee.ui.signup;



import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.reactivestreams.Subscription;

import javax.inject.Inject;


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
        mDataManager.getFirebaseService().signUp(email, password);
    }

}
