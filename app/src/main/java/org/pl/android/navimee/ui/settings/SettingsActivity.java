package org.pl.android.navimee.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
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
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if (drawerItem instanceof Nameable) {
                Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);
            } else {
                Log.i("material-drawer", "toggleChecked: " + isChecked);
            }

            /**
             * We load a PreferenceFragment which is the recommended way by Android
             * see @http://developer.android.com/guide/topics/ui/settings.html#Fragment
             * @TargetApi(11)
             */
            // ((BoilerplateApplication) getApplication()).get.inject(SettingsActivity.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_user);
        mSettingsPresenter.attachView(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        final IProfile profile;
        if (mSettingsPresenter.getName() != null) {
            profile = new ProfileDrawerItem().withName(mSettingsPresenter.getName()).withEmail(mSettingsPresenter.getEmail());
        } else {
            profile = new ProfileDrawerItem().withEmail(mSettingsPresenter.getEmail());
        }

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.md_dark_background)
                .withTranslucentStatusBar(false)
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.settings).withIcon(R.drawable.ic_action_whatshot).withIdentifier(1),
                        new PrimaryDrawerItem().withName("Uber").withIcon(R.drawable.ic_action_whatshot).withIdentifier(2),
                        new PrimaryDrawerItem().withName("User").withIcon(R.drawable.ic_action_whatshot).withIdentifier(3),
                        new PrimaryDrawerItem().withName(R.string.logout).withIcon(R.drawable.ic_action_whatshot).withIdentifier(4),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.privacy_conditions).withIcon(R.drawable.ic_action_whatshot),
                        new PrimaryDrawerItem().withName(R.string.help).withIcon(R.drawable.ic_action_whatshot),
                        new PrimaryDrawerItem().withName(R.string.rate_app).withIcon(R.drawable.ic_action_whatshot)
                )
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