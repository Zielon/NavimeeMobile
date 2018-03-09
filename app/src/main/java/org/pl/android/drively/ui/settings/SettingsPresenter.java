package org.pl.android.drively.ui.settings;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.service.GeolocationUpdateService;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.util.FirebasePaths;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

import static org.pl.android.drively.util.FirebasePaths.USERS;

public class SettingsPresenter extends BasePresenter<SettingsMvpView> {

    private final DataManager dataManager;
    private final UsersRepository usersRepository;
    private PreferencesHelper preferencesHelper;
    private Disposable disposable;
    private FirebaseUser firebaseUser;

    @Inject
    public SettingsPresenter(DataManager dataManager, PreferencesHelper preferencesHelper, UsersRepository usersRepository) {
        this.preferencesHelper = preferencesHelper;
        this.dataManager = dataManager;
        this.firebaseUser = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser();
        this.usersRepository = usersRepository;
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

        try {
            usersRepository.updateUserField(userId, "online", false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

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
}
