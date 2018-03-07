package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.contracts.repositories.NotificationsRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.EventNotification;
import org.pl.android.drively.util.FirebasePaths;

import java.util.UUID;

import javax.inject.Inject;

public class NotificationsRepositoryImpl implements NotificationsRepository {

    private DataManager dataManager;
    private String country;

    @Inject
    public NotificationsRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
        this.country = dataManager.getPreferencesHelper().getCountry();
    }

    @Override
    public Task<Void> addEventNotification(EventNotification eventNotification) {
        String uuid = eventNotification.getId() + "_" + dataManager.getPreferencesHelper().getUserId();
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.NOTIFICATIONS)
                .document(country)
                .collection(FirebasePaths.EVENTS_NOTIFICATION)
                .document(uuid).set(eventNotification);
    }

    @Override
    public Task<Void> deleteEventNotification(String notificationId) {
        return dataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FirebasePaths.NOTIFICATIONS)
                .document(dataManager.getPreferencesHelper().getCountry())
                .collection(FirebasePaths.EVENTS_NOTIFICATION)
                .document(notificationId)
                .delete();
    }
}
