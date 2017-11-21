package org.pl.android.navimee.ui.events;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.reactivestreams.Subscription;

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
        if(date.getDay() == 1) {
            day = "firstDay";
        } else if (date.getDay() == 2) {
            day = "secondDay";
        } else if (date.getDay() == 3) {
            day = "thirdDay";
        } else if (date.getDay() == 4) {
            day = "fourthDay";
        } else if (date.getDay() == 5) {
            day = "fifthDay";
        } else if (date.getDay() == 6) {
            day = "sixthDay";
        } else if (date.getDay() == 7) {
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

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(event.getId(), event);
        mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("day_schedule").child(userId).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getMvpView().onSuccessSave();
            }
        });

    }

}
