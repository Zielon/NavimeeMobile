package org.pl.android.navimee.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.pl.android.navimee.R;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-11-20.
 */

public class SettingsActivity extends com.fnp.materialpreferences.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,SettingsMvpView {

    @Inject
    SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    activityComponent().inject(this);

        /**
         * We load a PreferenceFragment which is the recommended way by Android
         * see @http://developer.android.com/guide/topics/ui/settings.html#Fragment
         * @TargetApi(11)
         */
        setPreferenceFragment(new MyPreferenceFragment());
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d(key);
        sharedPreferences.getAll();
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment {
        @Override
        public int addPreferencesFromResource() {
            return R.xml.preferences; // Your preference file
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Preference button = findPreference("logout");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    return true;
                }
            });
        }

    }
}