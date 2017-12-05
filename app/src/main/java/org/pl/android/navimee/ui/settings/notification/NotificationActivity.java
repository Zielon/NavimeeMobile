package org.pl.android.navimee.ui.settings.notification;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.SettingsPresenter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wojtek on 2017-12-05.
 */

public class NotificationActivity extends BaseActivity implements NotificationMvpView {


    @BindView(R.id.day_schedule_notifications_text)
    TextView mDayScheduleNotiText;
    @BindView(R.id.day_schedule_notifications_button)
    Switch mDayScheduleNotiSwitch;
    @BindView(R.id.big_events_notifications_text)
    TextView mBigEventsNotiText;
    @BindView(R.id.big_events_notifications_button)
    Switch mBigEventsNotiSwitch;


    @Inject
    NotificationPresenter mNotificationPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_settings);
        activityComponent().inject(this);

        ButterKnife.bind(this);
        mNotificationPresenter.attachView(this);
        mNotificationPresenter.loadNotificationConfig();
    }

    @Override
    public void setSwitches(boolean dayScheduleNotification, boolean bigEventsNotification) {
        mDayScheduleNotiSwitch.setChecked(dayScheduleNotification);
        mBigEventsNotiSwitch.setChecked(bigEventsNotification);
    }
}
