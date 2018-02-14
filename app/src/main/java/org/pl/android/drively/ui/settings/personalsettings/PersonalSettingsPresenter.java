package org.pl.android.drively.ui.settings.personalsettings;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class PersonalSettingsPresenter extends BasePresenter<PersonalSettingsMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public PersonalSettingsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(PersonalSettingsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void loadNotificationConfig() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        try {
            String dayScheduleNotificationField = nameof(User.class,"dayScheduleNotification");
            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) return;
                        Timber.d(document.getId() + " => " + document.getData());

                        boolean dayScheduleNotification = (Boolean) document.getData().get(dayScheduleNotificationField);
    //                    boolean bigEventsNotification = (Boolean) document.getData().get("bigEventsNotification");

                        getMvpView().setSwitches(dayScheduleNotification);
                    }
                });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void submitCheckedChange(String name, boolean checked) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId).update(name, checked)
                .addOnSuccessListener(aVoid -> {
                Timber.i("Checked changed ");
                })
                .addOnFailureListener(e -> {
                        Timber.e("Error saving event", e);
                });


    }
}
