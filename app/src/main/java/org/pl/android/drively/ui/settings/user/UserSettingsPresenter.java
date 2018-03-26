package org.pl.android.drively.ui.settings.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.services.GeoLocationUpdateService;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.signinup.SignActivity;
import org.pl.android.drively.util.Const;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.inject.Inject;

import static org.pl.android.drively.util.BitmapUtils.scaleDown;
import static org.pl.android.drively.util.ConstIntents.ACTION;
import static org.pl.android.drively.util.ConstIntents.PROVIDERS;
import static org.pl.android.drively.util.ConstIntents.REAUTHENTICATE;
import static org.pl.android.drively.util.FirebasePaths.AVATARS;

public class UserSettingsPresenter extends BasePresenter<UserSettingsChangeMvpView> {

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private PreferencesHelper preferencesHelper;
    private UserSettingsChangeMvpView userSettingsChangeMvpView;

    @Inject
    public UserSettingsPresenter(DataManager dataManager) {
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        this.firebaseStorage = dataManager.getFirebaseService().getFirebaseStorage();
        this.preferencesHelper = dataManager.getPreferencesHelper();
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

    public void loadAvatar() {
        User user = preferencesHelper.getUserInfo();
        FirebaseStorage.getInstance().getReference(String.format("%s/%s", AVATARS, user.getId()))
                .getBytes(Const.FIVE_MEGABYTE)
                .addOnSuccessListener(bytes -> {
                    Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    userSettingsChangeMvpView.reloadAvatar(avatar);
                }).addOnFailureListener(failure -> userSettingsChangeMvpView.onSuccess() /* show the default avatar */);
    }

    public void setNewAvatar(Bitmap bitmap) {
        User user = preferencesHelper.getUserInfo();
        String path = String.format("%s/%s", AVATARS, user.getId());
        FirebaseStorage.getInstance().getReference(String.format("%s/%s", AVATARS, user.getId())).delete()
                .addOnCompleteListener(task -> {
                    Bitmap scaledBitmap = scaleDown(bitmap, 200, 100);
                    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayStream);
                    RxFirebaseStorage.putBytes(firebaseStorage.getReference().child(path), byteArrayStream.toByteArray()).subscribe(
                            ready -> userSettingsChangeMvpView.reloadAvatar(scaledBitmap),
                            throwable -> userSettingsChangeMvpView.onError(throwable));
                });
    }

    public void deleteUser(ProgressDialog progressDialog) {
        // The service has to delete the user location for Firebase. Therefore, a user has to be login.
        GeoLocationUpdateService.stopService();

        Activity activity = (Activity) userSettingsChangeMvpView;

        // The user node will be removed by Cloud Functions
        firebaseUser.delete().addOnSuccessListener(result -> {
            progressDialog.dismiss();
            preferencesHelper.clear();
            userSettingsChangeMvpView.onUserDeleted();
        }).addOnFailureListener(task -> {
            progressDialog.dismiss();
            if (task instanceof FirebaseAuthRecentLoginRequiredException || task.getMessage().contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN")) {
                Intent intent = new Intent(activity, SignActivity.class);
                intent.putExtra(ACTION, REAUTHENTICATE);
                if (firebaseUser.getProviders() != null)
                    intent.putStringArrayListExtra(PROVIDERS, (ArrayList<String>) firebaseUser.getProviders());
                activity.startActivityForResult(intent, 0);
            } else
                Toast.makeText(activity, activity.getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
        });
    }
}