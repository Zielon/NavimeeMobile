package org.pl.android.drively.ui.settings.user;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.ExternalProviders;
import org.pl.android.drively.util.FirebasePaths;

import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;

public class UserSettingsPresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private UserSettingsChangeMvpView _mvpView;

    @Inject
    public UserSettingsPresenter(DataManager dataManager) {
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        this.firebaseStorage = dataManager.getFirebaseService().getFirebaseStorage();
        this.firebaseFirestore = dataManager.getFirebaseService().getFirebaseFirestore();
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


    public Task<QuerySnapshot> getAvatarQuery(){
        return firebaseFirestore.collection(FirebasePaths.USERS).whereEqualTo("email", firebaseUser.getEmail()).get();
    }

    public StorageReference getStorageReference(String avatar ){
        return firebaseStorage.getReference().child(String.format("%s/%s", AVATARS, avatar));
    }

    public void setNewAvatar(Uri uri, User user){
        String avatar = this.firebaseUser.getEmail().replace('.', '_');
        user.setAvatar(avatar);
        String path = String.format("%s/%s", AVATARS, avatar);
        RxFirebaseStorage.putFile(firebaseStorage.getReference().child(path), uri)
                .subscribe(
                        sub -> firebaseFirestore.collection(FirebasePaths.USERS)
                                .document(user.getId()).set(user)
                                .addOnSuccessListener(task -> _mvpView.reloadAvatar()),
                        throwable -> _mvpView.onError());
    }

    public boolean isExternalProvider() {
        List<String> actualProviders = firebaseUser.getProviders();
        if (actualProviders == null) return false;

        return ListUtils.intersection(actualProviders, ExternalProviders.getExternalProviders()).size() > 0;
    }
}
