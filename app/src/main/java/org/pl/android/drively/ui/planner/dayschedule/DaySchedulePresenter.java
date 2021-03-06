package org.pl.android.drively.ui.planner.dayschedule;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.pl.android.drively.contracts.repositories.NotificationsRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.remote.FirebaseAnalyticsService;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.FirebasePaths;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class DaySchedulePresenter extends BasePresenter<DayScheduleMvpView> {
    private final DataManager mDataManager;
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    private ListenerRegistration mListener;
    private NotificationsRepository notificationsRepository;

    @Inject
    public DaySchedulePresenter(DataManager dataManager, NotificationsRepository notificationsRepository,
                                FirebaseAnalyticsService firebaseAnalyticsService) {
        this.mDataManager = dataManager;
        this.notificationsRepository = notificationsRepository;
        this.firebaseAnalyticsService = firebaseAnalyticsService;
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
        dt.add(Calendar.HOUR, 5);
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

        try {
            String endTimeFilter = nameof(Event.class, "endTime");
            String rankFilter = nameof(Event.class, "rank");

            String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
            mDataManager.getFirebaseService().getFirebaseFirestore()
                    .collection(FirebasePaths.NOTIFICATIONS)
                    .document(mDataManager.getPreferencesHelper().getCountry())
                    .collection(FirebasePaths.EVENTS_NOTIFICATION)
                    .whereEqualTo("userId", userId)
                    .whereGreaterThan(endTimeFilter, dateFinal)
                    .whereLessThan(endTimeFilter, dt.getTime())
                    .orderBy(endTimeFilter).orderBy(rankFilter).get()
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
                                    getMvpView().showEvents(eventList, dateTime);
                                }
                            }
                        } else {
                            Timber.e("Error getting documents: ", task.getException());
                        }
                    });

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void deleteEvent(Event event) {
        notificationsRepository.deleteEventNotification(event.getFirestoreId()).addOnSuccessListener(aVoid -> {
            if (getMvpView() != null)
                getMvpView().onSuccessDelete(event);
        }).addOnFailureListener(e -> {
        });
    }

    public void logAnalytics(String id, String name, Object content) {
        firebaseAnalyticsService.reportEvent(id, name, content);
    }
}