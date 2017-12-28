package org.pl.android.navimee.ui.settings.notification;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.base.BasePresenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class NotificationPresenter extends BasePresenter<NotificationMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public NotificationPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(NotificationMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void loadNotificationConfig() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()) return;
                    Timber.d(document.getId() + " => " + document.getData());

                    boolean dayScheduleNotification = (Boolean) document.getData().get("dayScheduleNotification");
                    boolean bigEventsNotification =  (Boolean) document.getData().get("bigEventsNotification");

                    getMvpView().setSwitches(dayScheduleNotification, bigEventsNotification);
                }
            }
        });
    }

    public void submitCheckedChange(String name, boolean checked) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).update(name, checked).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.i("Checked changed ");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("TimberArgCount")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.e("Error saving event", e);
                    }
                });


    }
}
