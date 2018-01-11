package org.pl.android.navimee.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.local.PreferencesHelper;

import org.pl.android.navimee.injection.ApplicationContext;
import org.pl.android.navimee.injection.module.ApplicationModule;
import org.pl.android.navimee.notifications.MyFirebaseInstanceIDService;
import org.pl.android.navimee.notifications.MyFirebaseMessagingService;
import org.pl.android.navimee.ui.chat.service.FriendChatService;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MyFirebaseInstanceIDService myFirebaseInstanceIDService);
    void inject(MyFirebaseMessagingService myFirebaseMessagingService);
    void inject(FriendChatService friendChatService);

    @ApplicationContext Context context();
    Application application();
    PreferencesHelper preferencesHelper();
    DataManager dataManager();
}
