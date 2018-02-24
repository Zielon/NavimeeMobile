package org.pl.android.drively.ui.settings.personalsettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class SettingsPreferencesActivity extends AppCompatPreferenceActivity implements SettingsPreferencesMvpView {

    private static final String TAG = SettingsPreferencesActivity.class.getSimpleName();

    @Inject
    SettingsPreferencesPresenter settingsPreferencesPresenter;
    Context context;


    private Preference.OnPreferenceChangeListener preferenceChangeListener = (preference, newValue) -> {
        settingsPreferencesPresenter.updatePreference(preference, newValue);
        return true;
    };

    private void bindPreferenceToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        BoilerplateApplication.get(this).getComponent().inject(this);
        this.context = this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class MainPreferenceFragment extends PreferenceFragment {

        private Map<String, Preference> preferences = new HashMap<>();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            updateAbout();

            preferences.put("chatPrivateNotification", findPreference("chatPrivateNotification"));
            preferences.put("chatGroupNotification", findPreference("chatGroupNotification"));
            preferences.put("dayScheduleNotification", findPreference("dayScheduleNotification"));

            for (Map.Entry<String, Preference> entry : preferences.entrySet()) {
                entry.getValue().setEnabled(false);
                bindPreferenceToValue(entry.getValue());
            }

            settingsPreferencesPresenter.setPreferences(preferences);
        }

        private void updateAbout(){
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
                findPreference("version").setSummary(String.format("Drively %s", packageInfo.versionName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
