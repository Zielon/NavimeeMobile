package org.pl.android.navimee.ui.settings;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.main.MainMvpView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created by Wojtek on 2017-11-20.
 */
@ConfigPersistent
public class SettingsPresenter extends BasePresenter<SettingsMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public SettingsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(SettingsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }


    public void logout() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Map<String,Object> updates = new HashMap<>();
        updates.put("token", FieldValue.delete());
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).update(updates);
        mDataManager.getFirebaseService().getFirebaseAuth().signOut();
        getMvpView().onLogout();
    }

    public String getName() {
        return  mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getDisplayName();
    }

    public String getEmail() {
        return  mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getEmail();
    }

}
