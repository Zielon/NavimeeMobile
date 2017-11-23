package org.pl.android.navimee.ui.events;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.reactivestreams.Subscription;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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


    public void loadEvents(Date date) {
        String day = "";
        Date today = Calendar.getInstance().getTime();
        DateTime selectedTime = new DateTime(date);
        DateTime todayTime = new DateTime(today);
        int compare =  Days.daysBetween(todayTime.toLocalDate(), selectedTime.toLocalDate()).getDays();
        if(compare == 0) {
            day = "firstDay";
        } else if (compare == 1) {
            day = "secondDay";
        } else if (compare == 2) {
            day = "thirdDay";
        } else if (compare == 3) {
            day = "fourthDay";
        } else if (compare == 4) {
            day = "fifthDay";
        } else if (compare == 5) {
            day = "sixthDay";
        } else if (compare == 6) {
            day = "seventhDay";
        }
        String city = mDataManager.getPreferencesHelper().getValueString(Const.LAST_LOCATION);
        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("segregatedEvents").document(city).collection(day).document("events").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e(e.getMessage());
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    getMvpView().showEvents(documentSnapshot.getData());
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


    public void saveEvent(Event event) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event", event);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("users").document(userId).collection("userEvents").document(event.getId()).set(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.i("Event saved");
                getMvpView().onSuccessSave();
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
