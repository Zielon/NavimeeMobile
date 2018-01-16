package org.pl.android.drively.ui.events;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;


import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-21.
 */
@ConfigPersistent
public class EventsPresenter extends BasePresenter<EventsMvpView> {
    private final DataManager mDataManager;

    public Subscription mSubscription;

    private ListenerRegistration mListener;

    private List<Event> eventList = new ArrayList<Event>();

    private Set<String> eventsKeyList = new HashSet<>();


    private List<Event> dayScheduleList = new ArrayList<>();

    @Inject
    public EventsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(EventsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public void loadEvents(Date date, String key) {

        eventsKeyList.add(key);

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
        String city = mDataManager.getPreferencesHelper().getValueString(Const.LAST_LOCATION);

        Date finalDateFinal = dateFinal;
        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("HOTSPOT").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e("Listen failed.", e);
                    return;
                }
                eventsKeyList.remove(snapshot.getId());
                if (snapshot != null && snapshot.exists() && snapshot.get("hotspotType").equals(Const.HotSpotType.EVENT.name())) {
                        Event event = snapshot.toObject(Event.class);
                        if (event.getEndTime() !=null && event.getEndTime().after(finalDateFinal) && event.getEndTime().before(dt.getTime())) {
                           /* if(getMvpView() != null) {
                                getMvpView().showEvent(snapshot.toObject(Event.class));
                            }*/
                            eventList.add(event);
                        }
                 }
                 if(eventsKeyList.isEmpty()) {
                     if(getMvpView() != null) {
                         if(eventList.isEmpty()) {
                             getMvpView().showEventsEmpty();
                         } else {
                             Collections.sort(eventList);
                             getMvpView().showEvents(eventList);
                         }
                     }
                 }

            }
        });

       /* RxFirebaseDatabase.observeValueEvent(mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("events"),DataSnapshotMapper.listOf(Event.class))
              .subscribe(event -> {
                  if (!event.isEmpty()) {
                      Collections.sort(event);
                      getMvpView().showEvents(event);
                  } else {
                      getMvpView().showEventsEmpty();
                  }
              }, throwable -> {
                  Timber.e("RxFirebaseDatabase", throwable.toString());
              });*/
    }


    public void loadDayScheduleEvents() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("NOTIFICATIONS").whereEqualTo("userId",userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e("Listen failed.", e);
                    return;
                }
                setDayScheduleList(value.toObjects(Event.class));
            }
        });
    }


    public void saveEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("startTime", event.getStartTime());
        eventMap.put("endTime", event.getEndTime());
        eventMap.put("hotspotType", event.getHotspotType().name());
        eventMap.put("userId",userId);
        eventMap.put("id",event.getId());
        eventMap.put("title",event.getTitle());
        eventMap.put("rank",event.getRank());
        eventMap.put("isSent",false);
        Map<String, Object> place = new HashMap<>();
        place.put("address", event.getPlace().getAddress());
        place.put("category", event.getPlace().getCategory());
        place.put("city", event.getPlace().getCity());
        place.put("geoPoint",event.getPlace().getGeoPoint());
        place.put("id", event.getPlace().getId());
        place.put("name", event.getPlace().getName());
        eventMap.put("place",place);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("NOTIFICATIONS").document(event.getId()).set(eventMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.i("Event saved");
                if(getMvpView() != null) {
                    getMvpView().onSuccessSave();
                }
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


    public DatabaseReference getHotSpotDatabaseRefernce() {
        return mDataManager.getFirebaseService().getFirebaseDatabase().getReference("HOTSPOT");
    }

    public double getLastLat() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LAT);
    }

    public double getLastLng() {
        return mDataManager.getPreferencesHelper().getValueFloat(Const.LAST_LOCATION_LNG);
    }

    public List<Event> getDayScheduleList() {
        return dayScheduleList;
    }

    public void setDayScheduleList(List<Event> dayScheduleList) {
        this.dayScheduleList = dayScheduleList;
    }

    public Set<String> getEventsKeyList() {
        return eventsKeyList;
    }

    public void setEventsKeyList(Set<String> eventsKeyList) {
        this.eventsKeyList = eventsKeyList;
    }

    public void clearEvents() {
        eventList.clear();
    }
}
