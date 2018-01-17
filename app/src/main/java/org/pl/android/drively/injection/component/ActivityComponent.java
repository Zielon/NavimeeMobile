package org.pl.android.drively.injection.component;

import android.content.Context;

import dagger.Subcomponent;

import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.injection.PerActivity;
import org.pl.android.drively.injection.module.ActivityModule;
import org.pl.android.drively.ui.chat.ChatFragment;
import org.pl.android.drively.ui.chat.addgroup.AddGroupActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.friends.FriendsFragment;
import org.pl.android.drively.ui.chat.group.GroupFragment;
import org.pl.android.drively.ui.dayschedule.DayScheduleFragment;
import org.pl.android.drively.ui.events.EventsFragment;
import org.pl.android.drively.ui.hotspot.HotSpotFragment;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.ui.settings.personalsettings.PersonalSettingsActivity;
import org.pl.android.drively.ui.settings.SettingsActivity;
import org.pl.android.drively.ui.settings.user.name.UserNameChangeActivity;
import org.pl.android.drively.ui.settings.user.password.UserPasswordChangeActivity;
import org.pl.android.drively.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.drively.ui.settings.user.UserSettingsActivity;
import org.pl.android.drively.ui.settings.user.email.UserEmailChangeActivity;
import org.pl.android.drively.ui.signin.SignInActivity;
import org.pl.android.drively.ui.signup.SignUpActivity;
import org.pl.android.drively.ui.welcome.WelcomeActivity;

@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);
    void inject(PersonalSettingsActivity notificationActivity);
    void inject(EventsFragment eventsFragment);
    void inject(HotSpotFragment hotSpotFragment);
    void inject(DayScheduleFragment dayScheduleFragment);
    void inject(GroupFragment groupFragment);
    void inject(FriendsFragment friendsFragment);
    void inject(SignInActivity signInActivity);
    void inject(SettingsActivity settingsActivity);
    void inject(SignUpActivity signUpActivity);
    void inject(WelcomeActivity welcomeActivity);
    void inject(UserEmailChangeActivity userEmailChangeActivity);
    void inject(ReauthenticateActivity reauthenticateActivity);
    void inject(UserSettingsActivity userSettingsActivity);
    void inject(UserNameChangeActivity userNameChangeActivity);
    void inject(UserPasswordChangeActivity userPasswordChangeActivity);
    void inject(ChatFragment chatFragment);
    void inject(ChatViewActivity chatViewActivity);
    void inject(AddGroupActivity addGroupActivity);

    @ActivityContext
    Context provideContext();
}
