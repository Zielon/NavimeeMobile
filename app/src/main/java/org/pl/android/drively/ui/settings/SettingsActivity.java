package org.pl.android.drively.ui.settings;

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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.regulations.RegulationsActivity;
import org.pl.android.drively.ui.settings.personalsettings.PersonalSettingsActivity;
import org.pl.android.drively.ui.settings.user.UserSettingsActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SettingsActivity extends BaseActivity implements SettingsMvpView {

    private static final int REQUEST_SETTINGS = 1;
    @Inject
    SettingsPresenter settingsPresenter;
    private Drawer drawer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_user);
        settingsPresenter.attachView(this);
        Drawable grayBackground = getResources().getDrawable(R.drawable.primary);

        TextView textView = (TextView) findViewById(R.id.text_user_name);
        textView.setText(settingsPresenter.getName());

        List<IDrawerItem> drawerItems = new ArrayList<>();

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.user)
                .withIcon(R.drawable.happy_user_24dp)
                .withIdentifier(2)
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.settings)
                .withIcon(R.drawable.settings_24dp)
                .withIdentifier(0)
                .withTextColor(getResources().getColor(R.color.white)));

/*        drawerItems.add(new PrimaryDrawerItem().withName("Uber")
                .withIcon(R.drawable.uber_icon_24dp)
                .withIdentifier(1)
                .withTextColor(getResources().getColor(R.color.white)));*/

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.logout)
                .withIcon(R.drawable.logout_24dp)
                .withIdentifier(3)
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new DividerDrawerItem().withEnabled(true));

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.privacy_conditions)
                .withIcon(R.drawable.legal_privacy_24dp)
                .withIdentifier(4)
                .withTextColor(getResources().getColor(R.color.white)));
/*
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.help)
                .withIcon(R.drawable.help_circle_24dp)
                .withIdentifier(5)
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.rate_app)
                .withIcon(R.drawable.rate_app_24dp)
                .withIdentifier(6)
                .withTextColor(getResources().getColor(R.color.white)));*/

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .addDrawerItems(drawerItems.toArray(new IDrawerItem[drawerItems.size()]))
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem instanceof Nameable) {
                        Intent intent = null;
                        position = (int) drawerItem.getIdentifier();
                        if (position == 0) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, PersonalSettingsActivity.class);
                        } else if (position == 1) {
                            Timber.d(String.valueOf(position));
                        } else if (position == 2) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, UserSettingsActivity.class);
                        } else if (position == 3) {
                            settingsPresenter.logout();
                        } else if (position == 4) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, RegulationsActivity.class);
                        }
                        if (intent != null) {
                            SettingsActivity.this.startActivityForResult(intent, REQUEST_SETTINGS);
                        }
                    }
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(-1)
                .buildView();

        drawer.getSlider().setBackground(grayBackground);

        ((ViewGroup) findViewById(R.id.frame_container)).addView(drawer.getSlider());
    }

    @Override
    public void onSuccess() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        drawer.setSelection(-1);
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