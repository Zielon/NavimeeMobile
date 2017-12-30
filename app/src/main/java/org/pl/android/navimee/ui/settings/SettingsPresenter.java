package org.pl.android.navimee.ui.settings;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.ExternalProviders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

import static org.pl.android.navimee.util.FirebasePaths.USERS;

@ConfigPersistent
public class SettingsPresenter extends BasePresenter<SettingsMvpView> {

    private final DataManager dataManager;
    private Disposable disposable;
    private FirebaseUser firebaseUser;

    @Inject
    public SettingsPresenter(DataManager dataManager) {
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

        getMvpView().onLogout();
    }

    public String getName() {
        return firebaseUser.getDisplayName();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }

    public boolean isExternalProvider() {
        List<String> actualProviders = firebaseUser.getProviders();
        if (actualProviders == null) return false;

        return ListUtils.intersection(actualProviders, ExternalProviders.getExternalProviders()).size() > 0;
    }
}
