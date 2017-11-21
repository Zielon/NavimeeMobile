package org.pl.android.navimee.ui.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.BaseBundle;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.pl.android.navimee.BoilerplateApplication;
import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-11-20.
 */

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, SettingsMvpView {

    @Inject
    SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_settings);
        mSettingsPresenter.attachView(this);

        /**
         * We load a PreferenceFragment which is the recommended way by Android
         * see @http://developer.android.com/guide/topics/ui/settings.html#Fragment
         * @TargetApi(11)
         */
       // ((BoilerplateApplication) getApplication()).get.inject(SettingsActivity.this);
        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new MyPreferenceFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d(key);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onLogout() {
        finish();
    }

    @Override
    public void onError() {

    }

    @SuppressLint("ValidFragment")
    public class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            Preference button = findPreference("logout");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    mSettingsPresenter.logout();
                    return true;
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSettingsPresenter.detachView();
    }
}