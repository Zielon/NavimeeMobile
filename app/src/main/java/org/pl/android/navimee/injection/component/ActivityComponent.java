package org.pl.android.navimee.injection.component;

import dagger.Subcomponent;
import org.pl.android.navimee.injection.PerActivity;
import org.pl.android.navimee.injection.module.ActivityModule;
import org.pl.android.navimee.ui.events.EventsFragment;
import org.pl.android.navimee.ui.main.MainActivity;
import org.pl.android.navimee.ui.signin.SignInActivity;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);
    void inject(EventsFragment eventsFragment);
    void inject(SignInActivity signInActivity);

}
