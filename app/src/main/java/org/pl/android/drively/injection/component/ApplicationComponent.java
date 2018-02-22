package org.pl.android.drively.injection.component;

import android.app.Application;
import android.content.Context;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.injection.module.ApplicationModule;
import org.pl.android.drively.notifications.MyFirebaseInstanceIDService;
import org.pl.android.drively.notifications.MyFirebaseMessagingService;
import org.pl.android.drively.service.GeolocationUpdateService;
import org.pl.android.drively.ui.settings.personalsettings.SettingsPreferencesActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MyFirebaseInstanceIDService myFirebaseInstanceIDService);

    void inject(MyFirebaseMessagingService myFirebaseMessagingService);

    void inject(GeolocationUpdateService geolocationUpdateService);

    void inject(SettingsPreferencesActivity settingsPreferencesActivity);

    @ApplicationContext
    Context context();

    Application application();

    PreferencesHelper preferencesHelper();

    DataManager dataManager();
}
