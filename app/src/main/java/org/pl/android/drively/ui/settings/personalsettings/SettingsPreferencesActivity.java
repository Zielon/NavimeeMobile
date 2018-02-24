package org.pl.android.drively.ui.settings.personalsettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SettingsPreferencesActivity extends AppCompatPreferenceActivity implements SettingsPreferencesMvpView {

    @Inject
    SettingsPreferencesPresenter settingsPreferencesPresenter;
    @Inject
    DataManager dataManager;
    private List<String> settings = new ArrayList<>();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BoilerplateApplication.get(this).getComponent().inject(this);

        // Settings form the user model
        settings.add("shareLocalization");
        settings.add("chatPrivateNotification");
        settings.add("chatGroupNotification");
        settings.add("dayScheduleNotification");

        settingsPreferencesPresenter.updateSharedPreferences(settings, this);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
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

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            updateAbout();

            for (String setting : settings)
                settingsPreferencesPresenter.bindPreferenceToValue(findPreference(setting));
        }

        private void updateAbout() {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
                findPreference("version").setSummary(String.format("Drively %s", packageInfo.versionName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
