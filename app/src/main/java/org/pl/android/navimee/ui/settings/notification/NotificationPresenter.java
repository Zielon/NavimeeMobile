package org.pl.android.navimee.ui.settings.notification;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.settings.SettingsMvpView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created by Wojtek on 2017-12-05.
 */

public class NotificationPresenter extends BasePresenter<NotificationMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;
    private ListenerRegistration mListener;

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
        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                Timber.d(documentSnapshot.getId() + " => " + documentSnapshot.getData());
                documentSnapshot.getData().get("dayScheduleNotification");
                documentSnapshot.getData().get("bigEventsNotification");
                getMvpView().setSwitches( (Boolean)documentSnapshot.getData().get("dayScheduleNotification"), (Boolean)documentSnapshot.getData().get("bigEventsNotification"));
            }
        });
    }
}
