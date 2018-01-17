package org.pl.android.drively.ui.dayschedule;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

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
        if (!DateUtils.isToday(dateTime)) {
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
        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection("NOTIFICATIONS")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("endTime", dateFinal)
                .whereLessThan("endTime", dt.getTime()).orderBy("endTime").orderBy("rank").get()
                .addOnFailureListener(task -> {
                    if (getMvpView() != null) {
                        getMvpView().showEventsEmpty();
                    }
                })
                .addOnCompleteListener(task -> {
                    List<Event> eventList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            Event event = documentSnapshot.toObject(Event.class);
                            event.setFirestoreId(documentSnapshot.getId());
                            eventList.add(event);
                        }
                        if (getMvpView() != null) {
                            if (eventList.isEmpty()) {
                                getMvpView().showEventsEmpty();
                            } else {
                                getMvpView().showEvents(eventList);
                            }
                        }
                    } else {
                        Timber.e("Error getting documents: ", task.getException());
                    }
                });
    }

    public void deleteEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("NOTIFICATIONS").document(event.getFirestoreId()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (getMvpView() != null) {
                        getMvpView().onSuccessDelete(event);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }
}