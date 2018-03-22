package org.pl.android.drively.injection.component;

import android.app.Application;
import android.content.Context;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.injection.module.ApplicationModule;
import org.pl.android.drively.injection.module.RepositoriesModule;
import org.pl.android.drively.injection.module.ServicesModule;
import org.pl.android.drively.notifications.MyFirebaseInstanceIDService;
import org.pl.android.drively.notifications.MyFirebaseMessagingService;
import org.pl.android.drively.services.GeoLocationUpdateService;
import org.pl.android.drively.ui.settings.personalsettings.SettingsPreferencesActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, RepositoriesModule.class, ServicesModule.class})
public interface ApplicationComponent {

    void inject(MyFirebaseInstanceIDService myFirebaseInstanceIDService);

    void inject(MyFirebaseMessagingService myFirebaseMessagingService);

    void inject(GeoLocationUpdateService geolocationUpdateService);

    void inject(SettingsPreferencesActivity settingsPreferencesActivity);

    @ApplicationContext
    Context context();

    Application application();

    PreferencesHelper preferencesHelper();

    DataManager dataManager();
}
