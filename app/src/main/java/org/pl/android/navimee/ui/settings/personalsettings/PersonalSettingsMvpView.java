package org.pl.android.navimee.ui.settings.personalsettings;

import org.pl.android.navimee.ui.base.MvpView;

public interface PersonalSettingsMvpView extends MvpView {
    void setSwitches(boolean dayScheduleNotification, boolean bigEventsNotification);
}
