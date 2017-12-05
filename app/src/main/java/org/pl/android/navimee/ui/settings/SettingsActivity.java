package org.pl.android.navimee.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.notification.NotificationActivity;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-11-20.
 */

public class SettingsActivity extends BaseActivity implements SettingsMvpView {

    private static final int PROFILE_SETTING = 1;

    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Inject
    SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_user);
        mSettingsPresenter.attachView(this);


        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);


        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName(mSettingsPresenter.getName()).withEmail(mSettingsPresenter.getEmail());
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withTranslucentStatusBar(false)
                .addProfiles(
                        profile,
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account"),
                        new ProfileSettingDrawerItem().withName(R.string.delete_account)
                )
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.notifications).withIcon(R.drawable.ic_action_whatshot).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.change_email).withIcon(R.drawable.ic_action_whatshot).withIdentifier(2),
                        new PrimaryDrawerItem().withName(R.string.change_password).withIcon(R.drawable.ic_action_whatshot).withIdentifier(3),
                        new PrimaryDrawerItem().withName(R.string.logout).withIcon(R.drawable.ic_action_whatshot).withIdentifier(4),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.privacy_conditions).withIcon(R.drawable.ic_action_whatshot),
                        new PrimaryDrawerItem().withName(R.string.help).withIcon(R.drawable.ic_action_whatshot),
                        new PrimaryDrawerItem().withName(R.string.rate_app).withIcon(R.drawable.ic_action_whatshot)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            Intent intent = null;
                           if(position == 1) {
                               Timber.d(String.valueOf(position));
                               intent = new Intent(SettingsActivity.this,NotificationActivity.class);
                           } else if(position == 2) {
                               Timber.d(String.valueOf(position));
                           } else if(position == 3) {
                               Timber.d(String.valueOf(position));
                           } else if(position == 4) {
                               mSettingsPresenter.logout();
                           }

                            if (intent != null) {
                                SettingsActivity.this.startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(-1)
                .buildView();

        ((ViewGroup) findViewById(R.id.frame_container)).addView(result.getSlider());
    }

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
    public void onSuccess() {

    }

    @Override
   public void onLogout() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onError() {

    }


}