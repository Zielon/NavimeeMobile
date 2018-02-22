package org.pl.android.drively.ui.settings.personalsettings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.pl.android.drively.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class SettingsPreferencesActivity extends AppCompatPreferenceActivity implements SettingsPreferencesMvpView {

    private static final String TAG = SettingsPreferencesActivity.class.getSimpleName();

    @Inject
    static SettingsPreferencesPresenter settingsPreferencesPresenter;

    private static Preference.OnPreferenceChangeListener preferenceChangeListener = (preference, newValue) -> {
        settingsPreferencesPresenter.updatePreference(preference, newValue);
        return true;
    };

    private static void bindPreferenceToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        private Map<String, Preference> preferences = new HashMap<>();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            preferences.put("chatPrivateNotification", findPreference("chatPrivateNotification"));
            preferences.put("chatGroupNotification", findPreference("chatGroupNotification"));
            preferences.put("dayScheduleNotification", findPreference("dayScheduleNotification"));

            for (Map.Entry<String, Preference> entry : preferences.entrySet())
                entry.getValue().setEnabled(false);

            settingsPreferencesPresenter.setDefaultPreferences(preferences);
        }
    }
}
