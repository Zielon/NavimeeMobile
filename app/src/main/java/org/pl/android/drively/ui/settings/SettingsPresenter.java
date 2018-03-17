package org.pl.android.drively.ui.settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

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
        ChatViewActivity.bitmapAvatarUser = null;

        try {
            String userId = firebaseUser.getUid();
            usersRepository.updateUserField(userId, "online", false);
            usersRepository.deleteUserField(userId, "token");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // A user is already deleted
        }

        preferencesHelper.clear();
        preferencesHelper.unclearPopups();

        FirebaseAuth firebaseAuth = dataManager.getFirebaseService().getFirebaseAuth();
        if (firebaseAuth != null) firebaseAuth.signOut();

        getMvpView().onLogout();
    }

    public String getName() {
        return firebaseUser.getDisplayName();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }
}
