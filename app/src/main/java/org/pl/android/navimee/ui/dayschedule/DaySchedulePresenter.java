package org.pl.android.navimee.ui.dayschedule;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-30.
 */

@ConfigPersistent
public class DaySchedulePresenter extends BasePresenter<DayScheduleMvpView> {
    private final DataManager mDataManager;

    private ListenerRegistration mListener;


    @Inject
    public DaySchedulePresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(DayScheduleMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public void loadEvents(Date date) {
        // today
        Calendar dt = new GregorianCalendar();
// reset hour, minutes, seconds and millis
        dt.setTime(date);
        dt.set(Calendar.HOUR_OF_DAY, 0);
        dt.set(Calendar.MINUTE, 0);
        dt.set(Calendar.SECOND, 0);
        dt.set(Calendar.MILLISECOND, 0);

        // next day
        dt.add(Calendar.DAY_OF_MONTH, 1);
        DateTime dateTime = new DateTime(date);
        Date dateFinal = date;
        if(!DateUtils.isToday(dateTime)) {
            // today
            Calendar dt1 = new GregorianCalendar();
            // reset hour, minutes, seconds and millis
            dt1.setTime(date);
            dt1.set(Calendar.HOUR_OF_DAY, 0);
            dt1.set(Calendar.MINUTE, 0);
            dt1.set(Calendar.SECOND, 0);
            dt1.set(Calendar.MILLISECOND, 0);
            dateFinal = dt1.getTime();
        }
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();

        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("USER_EVENTS").whereGreaterThan("endTime",dateFinal).whereLessThan("endTime", dt.getTime())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        for (DocumentSnapshot document : documentSnapshots) {
                            Timber.d(document.getId() + " => " + document.getData());

                        }
                        getMvpView().showEvents(documentSnapshots.toObjects(Event.class));
                    }
                });
    };

    public void deleteEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("USER_EVENTS").document(event.getId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getMvpView().onSuccessDelete();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}