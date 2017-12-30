package org.pl.android.navimee.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.google.firebase.auth.FirebaseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Ribot;
import org.pl.android.navimee.ui.base.BaseActivityFragment;
import org.pl.android.navimee.ui.dayschedule.DayScheduleFragment;
import org.pl.android.navimee.ui.events.EventsFragment;
import org.pl.android.navimee.ui.hotspot.HotSpotFragment;
import org.pl.android.navimee.ui.intro.IntroActivity;
import org.pl.android.navimee.ui.signin.SignInActivity;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivityFragment implements MainMvpView {

    private static final String EXTRA_TRIGGER_SYNC_FLAG =
            "uk.co.ribot.androidboilerplate.ui.main.MainActivity.EXTRA_TRIGGER_SYNC_FLAG";



    @Inject MainPresenter mMainPresenter;
    @Inject RibotsAdapter mRibotsAdapter;
    static final int SETTINGS_REQUEST = 1;  // The request code
    boolean isFromNotification = false;
    double lat,lng;
    String name,count;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.app_bar);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

     /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(LOCATION_PERMS, Const.LOCATION_REQUEST);
            }
        }*/

        if(getIntent() != null &&  getIntent().getExtras() != null && getIntent().getExtras().get("lat") != null && getIntent().getExtras().get("lng") != null) {
      /*      Uri gmmIntentUri = Uri.parse("google.navigation:q=" + getIntent().getExtras().get("lat") + "," +
                    getIntent().getExtras().get("lng"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }*/
            isFromNotification = true;
            lat =  Double.valueOf(getIntent().getExtras().getString("lat"));
            lng =  Double.valueOf(getIntent().getExtras().getString("lng"));
            name = getIntent().getExtras().getString("name");
            count = getIntent().getExtras().getString("count");
        }

        checkAppIntro();


        checkLogin();
        mMainPresenter.attachView(this);


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                Fragment selectedFragment = null;
                switch (tabId) {
                    case R.id.tab_events:
                        selectedFragment = EventsFragment.newInstance();
                        break;
                    case R.id.tab_day_schedule:
                        selectedFragment = DayScheduleFragment.newInstance();
                        break;
                    case R.id.tab_hotspot:
                        selectedFragment = HotSpotFragment.newInstance();
                        if(isFromNotification) {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isFromNotification",true);
                            bundle.putDouble("lat", lat);
                            bundle.putDouble("lng", lng);
                            bundle.putString("name",name);
                            bundle.putString("count",count);
                            selectedFragment.setArguments(bundle);
                        }
                        break;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
            }
        });


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
                        @Override public void run() {
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
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }
    }



    /***** MVP View methods implementation *****/

    @Override
    public void showRibots(List<Ribot> ribots) {
        mRibotsAdapter.setRibots(ribots);
        mRibotsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showError() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void showRibotsEmpty() {
        mRibotsAdapter.setRibots(Collections.<Ribot>emptyList());
        mRibotsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
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


}
