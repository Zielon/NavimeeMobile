package org.pl.android.drively.injection.component;

import android.content.Context;

import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.injection.PerActivity;
import org.pl.android.drively.injection.module.ActivityModule;
import org.pl.android.drively.injection.module.RepositoriesModule;
import org.pl.android.drively.injection.module.ServicesModule;
import org.pl.android.drively.ui.chat.ChatFragment;
import org.pl.android.drively.ui.chat.addgroup.AddGroupActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.finance.FinanceFragment;
import org.pl.android.drively.ui.chat.friends.FriendsFragment;
import org.pl.android.drively.ui.chat.group.GroupFragment;
import org.pl.android.drively.ui.hotspot.HotSpotFragment;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.ui.planner.dayschedule.DayScheduleFragment;
import org.pl.android.drively.ui.planner.events.EventsFragment;
import org.pl.android.drively.ui.settings.SettingsActivity;
import org.pl.android.drively.ui.settings.user.UserSettingsActivity;
import org.pl.android.drively.ui.settings.user.email.UserEmailChangeActivity;
import org.pl.android.drively.ui.settings.user.name.UserNameChangeActivity;
import org.pl.android.drively.ui.settings.user.password.UserPasswordChangeActivity;
import org.pl.android.drively.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.drively.ui.signinup.SignActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = {ActivityModule.class, RepositoriesModule.class, ServicesModule.class})
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

    void inject(EventsFragment eventsFragment);

    void inject(HotSpotFragment hotSpotFragment);

    void inject(DayScheduleFragment dayScheduleFragment);

    void inject(GroupFragment groupFragment);

    void inject(FriendsFragment friendsFragment);

    void inject(SettingsActivity settingsActivity);

    void inject(SignActivity welcomeActivity);

    void inject(UserEmailChangeActivity userEmailChangeActivity);

    void inject(ReauthenticateActivity reauthenticateActivity);

    void inject(UserSettingsActivity userSettingsActivity);

    void inject(UserNameChangeActivity userNameChangeActivity);

    void inject(UserPasswordChangeActivity userPasswordChangeActivity);

    void inject(ChatFragment chatFragment);

    void inject(FinanceFragment financeFragment);

    void inject(ChatViewActivity chatViewActivity);

    void inject(AddGroupActivity addGroupActivity);

    @ActivityContext
    Context provideContext();
}
