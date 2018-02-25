package org.pl.android.drively.ui.base;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.injection.component.ActivityComponent;
import org.pl.android.drively.injection.component.ConfigPersistentComponent;
import org.pl.android.drively.injection.component.DaggerConfigPersistentComponent;
import org.pl.android.drively.injection.module.ActivityModule;

import java.util.concurrent.atomic.AtomicLong;

import timber.log.Timber;

/**
 * Abstract activity that every other Activity in this application must implement. It handles
 * creation of Dagger components and makes sure that instances of ConfigPersistentComponent survive
 * across configuration changes.
 */
public class BaseActivity extends AppCompatActivity {

    static final int SETTINGS_REQUEST = 1;  // The request code
    private static final String KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID";
    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private static final LongSparseArray<ConfigPersistentComponent> sComponentsMap = new LongSparseArray<>();

    private ActivityComponent mActivityComponent;
    private long mActivityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Create the ActivityComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        mActivityId = savedInstanceState != null ?
                savedInstanceState.getLong(KEY_ACTIVITY_ID) : NEXT_ID.getAndIncrement();

        mActivityComponent = getConfigPersistentComponent().activityComponent(new ActivityModule(this));
    }

    private ConfigPersistentComponent getConfigPersistentComponent() {
        ConfigPersistentComponent configPersistentComponent = sComponentsMap.get(mActivityId, null);

        if (configPersistentComponent == null) {
            Timber.i("Creating new ConfigPersistentComponent id=%d", mActivityId);

            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                    .applicationComponent(BoilerplateApplication.get(this).getComponent()).build();

            sComponentsMap.put(mActivityId, configPersistentComponent);
        }

        return configPersistentComponent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_ACTIVITY_ID, mActivityId);
    }

    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            Timber.i("Clearing ConfigPersistentComponent id=%d", mActivityId);
            sComponentsMap.remove(mActivityId);
        }
        super.onDestroy();
    }

    public ActivityComponent activityComponent() {
        if (mActivityComponent == null)
            mActivityComponent = getConfigPersistentComponent().activityComponent(new ActivityModule(this));

        return mActivityComponent;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }


}
