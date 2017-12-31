package org.pl.android.navimee.ui.settings.notification;

import org.pl.android.navimee.ui.base.MvpView;

public interface NotificationMvpView extends MvpView {
    void setSwitches(boolean dayScheduleNotification, boolean bigEventsNotification);
}
