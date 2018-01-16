package org.pl.android.drively.ui.settings.personalsettings;

import org.pl.android.drively.ui.base.MvpView;

public interface PersonalSettingsMvpView extends MvpView {
    void setSwitches(boolean dayScheduleNotification, boolean bigEventsNotification);
}
