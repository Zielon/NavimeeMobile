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
import org.pl.android.navimee.ui.welcome.WelcomeActivity;

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
    void inject(WelcomeActivity welcomeActivity);

    @ActivityContext
    Context provideContext();
}
