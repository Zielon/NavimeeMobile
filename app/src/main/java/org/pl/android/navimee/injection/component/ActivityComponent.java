package org.pl.android.navimee.injection.component;

import android.content.Context;

import dagger.Subcomponent;

import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.injection.PerActivity;
import org.pl.android.navimee.injection.module.ActivityModule;
import org.pl.android.navimee.ui.dayschedule.DayScheduleFragment;
import org.pl.android.navimee.ui.events.EventsFragment;
import org.pl.android.navimee.ui.hotspot.HotSpotFragment;
import org.pl.android.navimee.ui.main.MainActivity;
import org.pl.android.navimee.ui.settings.notification.NotificationActivity;
import org.pl.android.navimee.ui.settings.SettingsActivity;
import org.pl.android.navimee.ui.signin.SignInActivity;
import org.pl.android.navimee.ui.signup.SignUpActivity;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);
    void inject(NotificationActivity notificationActivity);
    void inject(EventsFragment eventsFragment);
    void inject(HotSpotFragment hotSpotFragment);
    void inject(DayScheduleFragment dayScheduleFragment);
    void inject(SignInActivity signInActivity);
    void inject(SettingsActivity settingsActivity);
    void inject(SignUpActivity signUpActivity);

    @ActivityContext
    Context provideContext();



}
