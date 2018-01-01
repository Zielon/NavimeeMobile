package org.pl.android.navimee.ui.settings.user;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class UserSettingsActivity extends BaseActivity {

    private String userName;
    private String userEmail;
    private boolean external;
    private Drawer drawer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        Drawable grayBackground = getResources().getDrawable(R.drawable.primary);

        userName = getIntent().getStringExtra("NAME");
        userEmail = getIntent().getStringExtra("EMAIL");
        external = getIntent().getBooleanExtra("EXTERNAL", false);

        List<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(new PrimaryDrawerItem().withName(userName).withIcon(R.drawable.happy_user_24dp).withTextColor(getResources().getColor(R.color.white)));
        drawerItems.add(new PrimaryDrawerItem().withName(userEmail).withIcon(R.drawable.email_user_24dp).withTextColor(getResources().getColor(R.color.white)));

        if (!external)
            drawerItems.add(new PrimaryDrawerItem().withName("**********").withIcon(R.drawable.password_24dp).withTextColor(getResources().getColor(R.color.white)));

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .addDrawerItems(drawerItems.toArray(new IDrawerItem[drawerItems.size()]))
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem instanceof Nameable) {
                        Intent intent = null;
                        if (position == 0) {
                            Timber.d(String.valueOf(position));
                        } else if (position == 1) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(UserSettingsActivity.this, UserEmailChangeActivity.class);
                        } else if (position == 2) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(UserSettingsActivity.this, UserPasswordChangeActivity.class);
                        }

                        if (intent != null) {
                            UserSettingsActivity.this.startActivity(intent);
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
}