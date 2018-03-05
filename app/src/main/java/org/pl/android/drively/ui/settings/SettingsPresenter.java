package org.pl.android.drively.ui.settings;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.service.GeolocationUpdateService;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.USERS;
import static org.pl.android.drively.util.ReflectionUtil.nameof;

@ConfigPersistent
public class SettingsPresenter extends BasePresenter<SettingsMvpView> {

    private final DataManager dataManager;
    private PreferencesHelper preferencesHelper;
    private Disposable disposable;
    private FirebaseUser firebaseUser;

    @Inject
    public SettingsPresenter(DataManager dataManager, PreferencesHelper preferencesHelper) {
        this.preferencesHelper = preferencesHelper;
        this.dataManager = dataManager;
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
    }

    @Override
    public void attachView(SettingsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (disposable != null) disposable.dispose();
    }

    public void logout() {
        String userId = firebaseUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("token", FieldValue.delete());

        dataManager.getFirebaseService().getFirebaseFirestore().collection(USERS).document(userId).update(updates);
        dataManager.getFirebaseService().getFirebaseAuth().signOut();

        ChatViewActivity.bitmapAvatarUser = null;

        preferencesHelper.clear();

        getMvpView().onLogout();
    }

    public String getName() {
        return firebaseUser.getDisplayName();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }

    public void deleteGeolocation() {
        dataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.USER_LOCATION)
                .child(GeolocationUpdateService.FIREBASE_KEY).removeValue();

    }

    public String getDriverType() {
        return dataManager.getPreferencesHelper().getValueString(Const.DRIVER_TYPE);
    }

    public boolean getShareLocalisation() {
        return dataManager.getPreferencesHelper().getValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION);
    }

    public void updateShareLocalisationAndDriverType(String driverType, boolean shareLocalisation) {
        dataManager.getPreferencesHelper().setValue(Const.DRIVER_TYPE, driverType);
        dataManager.getPreferencesHelper().setValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalisation);
        try {
            updateUserField(dataManager.getPreferencesHelper().getUserId(), Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalisation);
            updateUserField(dataManager.getPreferencesHelper().getUserId(), "driverType", driverType);
        } catch (NoSuchFieldException e) {
            Timber.d(e);
        }
    }

    public Task<Void> updateUserField(String userId, String field, Object value) throws NoSuchFieldException {
        String filedToUpdate = nameof(User.class, field);
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId).update(filedToUpdate, value);
    }
}
