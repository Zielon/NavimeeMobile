package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.contracts.repositories.NotificationsRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.EventNotification;
import org.pl.android.drively.util.FirebasePaths;

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
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.NOTIFICATIONS)
                .document(country)
                .collection(FirebasePaths.EVENTS_NOTIFICATION)
                .document(eventNotification.getId())
                .set(eventNotification);
    }
}
