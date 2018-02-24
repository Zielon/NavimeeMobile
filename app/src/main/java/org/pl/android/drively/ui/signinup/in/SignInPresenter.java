package org.pl.android.drively.ui.signinup.in;

import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.signinup.BaseSignPresenter;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

import timber.log.Timber;

public class SignInPresenter extends BaseSignPresenter {

    @Inject
    public SignInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public void loginIn(String email, String password) {
        RxFirebaseAuth.signInWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(), email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    Timber.i("RxFirebaseSample", "user token: " + token.getToken());
                    mDataManager.getPreferencesHelper().setValue(Const.MESSAGING_TOKEN, token.getToken());
                    mMvpView.onSuccess();
                }, throwable -> {
                    Timber.e("RxFirebaseSample", throwable.toString());
                    mMvpView.onError(throwable);
                });
    }
}