package org.pl.android.navimee.ui.dayschedule;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.kelvinapps.rxfirebase.DataSnapshotMapper;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.ui.base.BasePresenter;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-30.
 */

@ConfigPersistent
public class DaySchedulePresenter extends BasePresenter<DayScheduleMvpView> {
    private final DataManager mDataManager;

    private Task<QuerySnapshot> mListener;


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
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();

        mListener = mDataManager.getFirebaseService().getFirebaseFirestore().collection("users").document(userId).collection("userEvents").whereGreaterThan("time", date)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("TimberArgCount")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Timber.d(document.getId() + " => " + document.getData());

                            }
                            getMvpView().showEvents(task.getResult().toObjects(Event.class));
                        } else {
                            Timber.e( "Error getting documents: ", task.getException());
                        }
                    }
                })
               .addOnFailureListener(new OnFailureListener() {
                   @SuppressLint("TimberArgCount")
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Timber.e( "Error getting documents: ", e);
                   }
               })
        ;



    }

    public void deleteEvent(Event event) {
        String key = mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("day_schedule").push().getKey();
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Query eventsQuery = mDataManager.getFirebaseService().getFirebaseDatabase().getReference().child("day_schedule").child(userId).orderByChild("name").equalTo(event.getName());

        eventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventsSnapshot: dataSnapshot.getChildren()) {
                    eventsSnapshot.getRef().removeValue();
                    getMvpView().onSuccessDelete();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e("onCancelled", databaseError.toException());
            }
        });

    }
}