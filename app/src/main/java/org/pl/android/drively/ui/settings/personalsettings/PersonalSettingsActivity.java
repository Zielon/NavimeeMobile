package org.pl.android.drively.ui.settings.personalsettings;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class PersonalSettingsActivity extends BaseActivity implements PersonalSettingsMvpView {

    @BindView(R.id.day_schedule_notifications_text)
    TextView mDayScheduleNotiText;
    @BindView(R.id.day_schedule_notifications_button)
    Switch mDayScheduleNotiSwitch;

    @Inject
    PersonalSettingsPresenter mNotificationPresenter;

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
    public void setSwitches(boolean dayScheduleNotification) {
        mDayScheduleNotiSwitch.setChecked(dayScheduleNotification);
    }

    @OnCheckedChanged({R.id.day_schedule_notifications_button})
    public void submitSwitchButton(CompoundButton button, boolean checked) {
        switch (button.getId()) {
            case R.id.day_schedule_notifications_button:
                mNotificationPresenter.submitCheckedChange("dayScheduleNotification", checked);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationPresenter.detachView();
    }
}
