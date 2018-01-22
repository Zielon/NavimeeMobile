package org.pl.android.drively.ui.settings.user;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.ExternalProviders;

import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;

public class UserSettingsPresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private UserSettingsChangeMvpView _mvpView;

    private String AVATAR_PATH;

    @Inject
    public UserSettingsPresenter(DataManager dataManager) {
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        this.firebaseStorage = dataManager.getFirebaseService().getFirebaseStorage();
        AVATAR_PATH = String.format("%s/%s", AVATARS, firebaseUser.getEmail());
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        _mvpView = mvpView;
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }

    public String getName() {
        return firebaseUser.getDisplayName();
    }

    public StorageReference getAvatarReference(){
        return firebaseStorage.getReference().child(AVATAR_PATH);
    }

    public void setNewAvatar(Uri uri){
        RxFirebaseStorage.putFile(firebaseStorage.getReference().child(AVATAR_PATH), uri)
                .subscribe(sub -> _mvpView.reloadAvatar(), throwable -> _mvpView.onError());
    }

    public boolean isExternalProvider() {
        List<String> actualProviders = firebaseUser.getProviders();
        if (actualProviders == null) return false;

        return ListUtils.intersection(actualProviders, ExternalProviders.getExternalProviders()).size() > 0;
    }
}
