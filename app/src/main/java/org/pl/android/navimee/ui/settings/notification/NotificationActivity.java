package org.pl.android.navimee.ui.settings.notification;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

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

    @OnCheckedChanged({R.id.day_schedule_notifications_button, R.id.big_events_notifications_button})
    public void submitSwitchButton(CompoundButton button, boolean checked) {
        switch (button.getId()) {
            case R.id.day_schedule_notifications_button:
                mNotificationPresenter.submitCheckedChange("dayScheduleNotification", checked);
                break;
            case R.id.big_events_notifications_button:
                mNotificationPresenter.submitCheckedChange("bigEventsNotification", checked);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationPresenter.detachView();
    }
}
