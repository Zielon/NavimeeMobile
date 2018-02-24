package org.pl.android.drively.ui.signinup.up;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kelvinapps.rxfirebase.RxFirebaseAuth;
import com.kelvinapps.rxfirebase.RxFirebaseUser;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.signinup.BaseSignPresenter;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

import timber.log.Timber;

public class SignUpPresenter extends BaseSignPresenter {

    @Inject
    public SignUpPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public void register(String email, String password, String name) {
        RxFirebaseAuth.createUserWithEmailAndPassword(mDataManager.getFirebaseService().getFirebaseAuth(), email, password)
                .flatMap(x -> RxFirebaseUser.getToken(mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser(), false))
                .subscribe(token -> {
                    FirebaseUser user = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
                    mDataManager.getPreferencesHelper().setValue(Const.MESSAGING_TOKEN, token.getToken());
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    RxFirebaseUser.updateProfile(user, profile).subscribe(sub -> mMvpView.onSuccess());
                }, throwable -> mMvpView.onError(throwable));
    }
}
