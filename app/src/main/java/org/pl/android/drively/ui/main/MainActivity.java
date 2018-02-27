package org.pl.android.drively.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.eventbus.NotificationEvent;
import org.pl.android.drively.ui.base.BaseActivityFragment;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.chat.ChatFragment;
import org.pl.android.drively.ui.chat.finance.FinanceFragment;
import org.pl.android.drively.ui.hotspot.HotSpotFragment;
import org.pl.android.drively.ui.intro.IntroActivity;
import org.pl.android.drively.ui.planner.PlannerFragment;
import org.pl.android.drively.ui.settings.SettingsActivity;
import org.pl.android.drively.ui.welcome.WelcomeActivity;
import org.pl.android.drively.util.NetworkUtil;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivityFragment implements MainMvpView {

    static final int SETTINGS_REQUEST = 1;  // The request code
    private static final String EXTRA_TRIGGER_SYNC_FLAG =
            "uk.co.ribot.androidboilerplate.ui.main.MainActivity.EXTRA_TRIGGER_SYNC_FLAG";
    @Inject
    MainPresenter mMainPresenter;
    boolean isFromNotification = false;
    double lat, lng;
    String name, count;
    BottomBar bottomBar;

    private BaseTabFragment selectedFragment;

    // @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    /**
     * Return an Intent to start this Activity.
     * triggerDataSyncOnCreate allows disabling the background sync service onCreate. Should
     * only be set to false during testing.
     */
    public static Intent getStartIntent(Context context, boolean triggerDataSyncOnCreate) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_TRIGGER_SYNC_FLAG, triggerDataSyncOnCreate);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getExtras() != null && intent.getExtras().get("lat") != null && intent.getExtras().get("lng") != null) {
            double lat = Double.valueOf(intent.getExtras().getString("lat"));
            double lng = Double.valueOf(intent.getExtras().getString("lng"));
            String name = intent.getExtras().getString("name");
            String count = intent.getExtras().getString("count");
            EventBus.getDefault().post(new NotificationEvent(lat, lng, name, count));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityComponent().inject(this);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.app_bar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        TextView text = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.app_bar_text);
        text.setWidth(size.x);
        text.setGravity(Gravity.LEFT);

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }*/

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().get("lat") != null && getIntent().getExtras().get("lng") != null) {
            isFromNotification = true;
            lat = Double.valueOf(getIntent().getExtras().getString("lat"));
            lng = Double.valueOf(getIntent().getExtras().getString("lng"));
            name = getIntent().getExtras().getString("name");
            count = getIntent().getExtras().getString("count");
        }

        mMainPresenter.attachView(this);
        mMainPresenter.checkVersion();

        checkAppIntro();
        checkLogin();

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                selectedFragment = null;
                switch (tabId) {
                    case R.id.tab_events:
                        selectedFragment = PlannerFragment.newInstance();
                        break;
                    case R.id.tab_hotspot:
                        selectedFragment = HotSpotFragment.newInstance();
                        if (isFromNotification) {
                            isFromNotification = false;
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isFromNotification", true);
                            bundle.putDouble("lat", lat);
                            bundle.putDouble("lng", lng);
                            bundle.putString("name", name);
                            bundle.putString("count", count);
                            ((HotSpotFragment)selectedFragment).setArguments(bundle);
                        }
                        break;
                    case R.id.tab_chat:
                        selectedFragment = ChatFragment.newInstance();
                        break;
                    case R.id.tab_finance:
                        selectedFragment = FinanceFragment.newInstance();
                        break;

                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commitAllowingStateLoss();
            }
        });
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkInternetConnectivity();
    }

    private void checkInternetConnectivity() {
        if (!NetworkUtil.isNetworkConnected(this)) {
            MaterialDialog dialogAlert = new MaterialDialog.Builder(this)
                    .title(R.string.network_lack)
                    .backgroundColor(getResources().getColor(R.color.primary_dark))
                    .contentColor(getResources().getColor(R.color.white))
                    .positiveText(R.string.close)
                    .negativeText(R.string.check_internet)
                    .onPositive((dialog, which) -> {
                        finish();
                    })
                    .onNegative((dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    })
                    .build();
            dialogAlert.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMainPresenter.detachView();
    }

    private void checkAppIntro() {
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //  If the activity has never started before...
                if (mMainPresenter.checkAppIntro()) {

                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                        }
                    });
                    mMainPresenter.setAppIntroShowed();

                }
            }
        });

        // Start the thread
        t.start();
    }

    private void checkLogin() {
        FirebaseUser user = mMainPresenter.checkLogin();
        if (user == null) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void showError() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                checkLogin();
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    @Override
    public void changeTabByResId(int tabResId) {
        bottomBar.selectTabWithId(tabResId);
    }

    public BottomBar getBottomBar() {
        return bottomBar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.user_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_REQUEST);
                return true;
            }
            case R.id.show_instruction: {
                selectedFragment.showInstructionPopup();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
