package org.pl.android.drively.ui.planner.events;

import com.google.firebase.database.DatabaseReference;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.pl.android.drively.contracts.repositories.NotificationsRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.EventNotification;
import org.pl.android.drively.ui.base.tab.BaseTabPresenter;
import org.pl.android.drively.ui.planner.EventHelper;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class EventsPresenter extends BaseTabPresenter<EventsMvpView> {

    private List<Event> eventList = new ArrayList<>();

    private List<Event> dayScheduleList = new ArrayList<>();

    private NotificationsRepository notificationsRepository;

    @Inject
    public EventsPresenter(DataManager dataManager, NotificationsRepository notificationsRepository) {
        this.mDataManager = dataManager;
        this.notificationsRepository = notificationsRepository;
    }

    @Override
    public void attachView(EventsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public void show(Date date) {
        if (getMvpView() != null)
            if (eventList.isEmpty()) {
                getMvpView().showEventsEmpty();
            } else {
                getMvpView().showEvents(EventHelper.sortEvents(eventList), new DateTime(date));
            }
    }

    public void add(Date date, Event event) {
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
        Date dateFinal = date;
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

        if (event.getEndTime() != null && event.getEndTime().after(dateFinal) && event.getEndTime().before(dt.getTime()))
            eventList.add(event);
    }

    public void loadDayScheduleEvents() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        String userIdFilter = null;
        try {
            userIdFilter = nameof(EventNotification.class, "userId");
            mDataManager.getFirebaseService().getFirebaseFirestore()
                    .collection(FirebasePaths.NOTIFICATIONS)
                    .document(mDataManager.getPreferencesHelper().getCountry())
                    .collection(FirebasePaths.EVENTS_NOTIFICATION)
                    .whereEqualTo(userIdFilter, userId)
                    .addSnapshotListener((value, e) -> {
                        if (e != null) {
                            Timber.e("Listen failed.", e);
                            return;
                        }
                        try {
                            setDayScheduleList(value.toObjects(Event.class));
                        } catch (Exception parse) {
                            Timber.e("Listen failed.", parse);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();

        EventNotification eventNotification = new EventNotification();
        eventNotification.setStartTime(event.getStartTime());
        eventNotification.setEndTime(event.getEndTime());
        eventNotification.setHotspotType(event.getHotspotType());
        eventNotification.setUserId(userId);
        eventNotification.setId(event.getId());
        eventNotification.setTitle(event.getTitle());
        eventNotification.setRank(event.getRank());
        eventNotification.setSent(false);
        eventNotification.setPlace(event.getPlace());

        notificationsRepository.addEventNotification(eventNotification)
                .addOnSuccessListener(success -> {
                    if (getMvpView() != null) {
                        getMvpView().onSuccessSave();
                    }
                }).addOnFailureListener(failure -> Timber.e("Error saving event", failure));
    }


    public DatabaseReference getHotSpotDatabaseRefernce() {
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference(FirebasePaths.HOTSPOT);
    }

    public double getLastLat() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LAT);
    }

    public double getLastLng() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LNG);
    }

    public void setDayScheduleList(List<Event> dayScheduleList) {
        this.dayScheduleList = dayScheduleList;
        if (getMvpView() != null) {
            getMvpView().updateDayScheduleListInAdapter(dayScheduleList);
        }
    }

    public void clearEvents() {
        eventList.clear();
    }


    public void deleteEvent(Event event) {
        notificationsRepository.deleteEventNotification(event.getFirestoreId()).addOnSuccessListener(aVoid -> {
            if (getMvpView() != null) {
                getMvpView().onSuccessDelete();
            }
        }).addOnFailureListener(e -> {
        });
    }
}
