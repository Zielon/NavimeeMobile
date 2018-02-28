package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.data.model.EventNotification;

public interface NotificationsRepository {
    Task<Void> addEventNotification(EventNotification eventNotification);
}
