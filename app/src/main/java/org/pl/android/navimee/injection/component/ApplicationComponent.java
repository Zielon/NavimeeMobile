package org.pl.android.navimee.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.SyncService;
import org.pl.android.navimee.data.local.DatabaseHelper;
import org.pl.android.navimee.data.local.PreferencesHelper;
import org.pl.android.navimee.data.remote.EventsService;
import org.pl.android.navimee.data.remote.RibotsService;
import org.pl.android.navimee.injection.ApplicationContext;
import org.pl.android.navimee.injection.module.ApplicationModule;
import org.pl.android.navimee.notifications.MyFirebaseInstanceIDService;
import org.pl.android.navimee.notifications.MyFirebaseMessagingService;
import org.pl.android.navimee.util.RxEventBus;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(SyncService syncService);
    void inject(MyFirebaseInstanceIDService myFirebaseInstanceIDService);
    void inject(MyFirebaseMessagingService myFirebaseMessagingService);

    @ApplicationContext Context context();
    Application application();
    RibotsService ribotsService();
    EventsService eventsService();
    PreferencesHelper preferencesHelper();
    DatabaseHelper databaseHelper();
    DataManager dataManager();
    RxEventBus eventBus();

}
