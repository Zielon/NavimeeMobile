package org.pl.android.navimee.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.notification.NotificationActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsActivity;

import javax.inject.Inject;

import timber.log.Timber;

public class SettingsActivity extends BaseActivity implements SettingsMvpView {

    @Inject
    SettingsPresenter settingsPresenter;
    private Drawer result = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_user);
        settingsPresenter.attachView(this);
        Drawable grayBackground = getResources().getDrawable(R.drawable.primary);

        TextView textView = (TextView) findViewById(R.id.text_user_name);
        textView.setText(settingsPresenter.getName());

        result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.settings).withIcon(R.drawable.settings_24dp).withIdentifier(1).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName("Uber").withIcon(R.drawable.uber_icon_24dp).withIdentifier(2).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.user).withIcon(R.drawable.happy_user_24dp).withIdentifier(3).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.logout).withIcon(R.drawable.logout_24dp).withIdentifier(4).withTextColor(getResources().getColor(R.color.white)),
                        new DividerDrawerItem().withEnabled(true),
                        new PrimaryDrawerItem().withName(R.string.privacy_conditions).withIcon(R.drawable.legal_privacy_24dp).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.help).withIcon(R.drawable.help_circle_24dp).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.rate_app).withIcon(R.drawable.rate_app_24dp).withTextColor(getResources().getColor(R.color.white))
                )
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem instanceof Nameable) {
                        Intent intent = null;
                        if (position == 0) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, NotificationActivity.class);
                        } else if (position == 1) {
                            Timber.d(String.valueOf(position));
                        } else if (position == 2) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, UserSettingsActivity.class);
                            intent.putExtra("EMAIL", settingsPresenter.getEmail());
                            intent.putExtra("NAME", settingsPresenter.getName());
                            intent.putExtra("EXTERNAL", settingsPresenter.isExternalProvider());
                        } else if (position == 3) {
                            settingsPresenter.logout();
                        }

                        if (intent != null) {
                            SettingsActivity.this.startActivity(intent);
                        }
                    }
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(-1)
                .buildView();

        result.getSlider().setBackground(grayBackground);

        ((ViewGroup) findViewById(R.id.frame_container)).addView(result.getSlider());
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onLogout() {
        Intent resultInt = new Intent();
        resultInt.putExtra("ACTION", "LOGOUT");
        setResult(Activity.RESULT_OK, resultInt);
        finish();
    }

    @Override
    public void onError() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingsPresenter.detachView();
    }
}