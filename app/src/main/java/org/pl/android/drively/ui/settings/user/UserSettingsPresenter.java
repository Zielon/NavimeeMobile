package org.pl.android.drively.ui.settings.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.ExternalProviders;
import org.pl.android.drively.util.FirebasePaths;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.InternalStorageManager.saveBitmap;

public class UserSettingsPresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private UserSettingsChangeMvpView userSettingsChangeMvpView;

    @Inject
    public UserSettingsPresenter(DataManager dataManager) {
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        this.firebaseStorage = dataManager.getFirebaseService().getFirebaseStorage();
        this.firebaseFirestore = dataManager.getFirebaseService().getFirebaseFirestore();
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {

        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());

        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    @Override
    public void attachView(UserSettingsChangeMvpView mvpView) {
        userSettingsChangeMvpView = mvpView;
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

    public StorageReference getStorageReference(String avatar) {
        return firebaseStorage.getReference().child(String.format("%s/%s", AVATARS, avatar));
    }

    public User setNewAvatar(Bitmap bitmap, User user, Context context) {
        String path = String.format("%s/%s", AVATARS, user.getAvatar());
        if (!user.getAvatar().equals(User.DEFAULT_AVATAR))
            RxFirebaseStorage.delete(firebaseStorage.getReference().child(path)).subscribe(success -> {
            }, throwable -> userSettingsChangeMvpView.onError(throwable));

        Date currentTime = Calendar.getInstance().getTime();
        String avatar = this.firebaseUser.getEmail().replace('.', '_') + "_" + currentTime.getTime();
        path = String.format("%s/%s", AVATARS, avatar);

        bitmap = scaleDown(bitmap, 200, true);

        Uri uri;

        try {
            uri = saveBitmap(user.getId(), bitmap, context);
        } catch (Exception e) {
            userSettingsChangeMvpView.onError(e);
            return user;
        }

        user.setAvatar(avatar);

        RxFirebaseStorage.putFile(firebaseStorage.getReference().child(path), uri)
                .subscribe(
                        sub -> firebaseFirestore.collection(FirebasePaths.USERS)
                                .document(user.getId()).update("avatar", user.getAvatar())
                                .addOnSuccessListener(task -> userSettingsChangeMvpView.reloadAvatar())
                                .addOnFailureListener(throwable -> userSettingsChangeMvpView.onError(throwable)),
                        throwable -> userSettingsChangeMvpView.onError(throwable));

        return user;
    }

    public boolean isExternalProvider() {
        List<String> actualProviders = firebaseUser.getProviders();
        if (actualProviders == null) return false;

        return ListUtils.intersection(actualProviders, ExternalProviders.getExternalProviders()).size() > 0;
    }
}
