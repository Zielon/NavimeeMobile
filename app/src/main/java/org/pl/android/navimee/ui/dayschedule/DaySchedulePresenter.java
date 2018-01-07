package org.pl.android.navimee.ui.dayschedule;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.Task;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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
        Date dateFinal = dateTime.toDate();
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
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("NOTIFICATIONS").whereEqualTo("userId",userId).whereGreaterThan("endTime",dateFinal).whereLessThan("endTime", dt.getTime()).get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Event> eventList = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        event.setFirestoreId(documentSnapshot.getId());
                        eventList.add(event);
                    }
                    if(getMvpView() != null) {
                        getMvpView().showEvents(eventList);
                    }
                } else {
                    Timber.e("Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void deleteEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("NOTIFICATIONS").document(event.getFirestoreId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(getMvpView() != null) {
                            getMvpView().onSuccessDelete(event);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}