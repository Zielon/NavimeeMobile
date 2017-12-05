package org.pl.android.navimee.ui.settings.notification;

import org.pl.android.navimee.ui.base.MvpView;

/**
 * Created by Wojtek on 2017-12-05.
 */

public interface NotificationMvpView extends MvpView {

    public void setSwitches(boolean dayScheduleNotification, boolean bigEventsNotification);
}
