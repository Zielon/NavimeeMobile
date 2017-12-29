package org.pl.android.navimee.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.notification.NotificationActivity;

import javax.inject.Inject;

import timber.log.Timber;

public class SettingsActivity extends BaseActivity implements SettingsMvpView {

    private static final int PROFILE_SETTING = 1;
    @Inject
    SettingsPresenter mSettingsPresenter;
    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private OnCheckedChangeListener onCheckedChangeListener = (drawerItem, buttonView, isChecked) -> {
        if (drawerItem instanceof Nameable) {
            Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);
        } else {
            Log.i("material-drawer", "toggleChecked: " + isChecked);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_user);
        mSettingsPresenter.attachView(this);

        Drawable grayBackground = getResources().getDrawable(R.drawable.primary);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setBackground(getResources().getDrawable(R.drawable.primary));
        toolbar.setNavigationIcon(R.drawable.ic_x___close);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.settings).withIcon(R.drawable.ic_settings).withIdentifier(1).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName("Uber").withIcon(R.drawable.ic_litera_u).withIdentifier(2).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName("User").withIcon(R.drawable.ic_emoticon_happy).withIdentifier(3).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.logout).withIcon(R.drawable.ic_login_variant).withIdentifier(4).withTextColor(getResources().getColor(R.color.white)),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.privacy_conditions).withIcon(R.drawable.ic_briefcase).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.help).withIcon(R.drawable.ic_help_circle).withTextColor(getResources().getColor(R.color.white)),
                        new PrimaryDrawerItem().withName(R.string.rate_app).withIcon(R.drawable.ic_thumb_up).withTextColor(getResources().getColor(R.color.white))
                )
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem instanceof Nameable) {
                        Intent intent = null;
                        if (position == 1) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(SettingsActivity.this, NotificationActivity.class);
                        } else if (position == 2) {
                            Timber.d(String.valueOf(position));
                        } else if (position == 3) {
                            Timber.d(String.valueOf(position));
                        } else if (position == 4) {
                            mSettingsPresenter.logout();
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
        mSettingsPresenter.detachView();
    }
}