package org.pl.android.drively.ui.settings.personalsettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.eventbus.HotspotSettingsChanged;
import org.pl.android.drively.service.GeolocationUpdateService;
import org.pl.android.drively.ui.hotspot.HotspotPopupHelper;
import org.pl.android.drively.util.Const;

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

    private MainPreferenceFragment mainPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BoilerplateApplication.get(this).getComponent().inject(this);

        // Settings form the user model
        settings.add(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION);
        settings.add(Const.SETTINGS_PREFERENCE_DRIVER_TYPE);
        settings.add(Const.SETTINGS_PREFERENCE_CHAT_PRIVATE_NOTIFICATION);
        settings.add(Const.SETTINGS_PREFERENCE_GROUP_NOTIFICATION);
        settings.add(Const.SETTINGS_PREFERENCE_DAY_SCHEDULE_NOTIFICATION);

        settingsPreferencesPresenter.updateSharedPreferences(settings, this);

        settingsPreferencesPresenter.attachView(this);
        mainPreferenceFragment = new MainPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mainPreferenceFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showAppropriatePopup(Preference preference) {
        if (preference.getKey().equals(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION)) {
            if (TextUtils.isEmpty(settingsPreferencesPresenter.getDriverType())
                    && !settingsPreferencesPresenter.getShareLocalization()) {
                showPopup(preference, true);
            }
            if (settingsPreferencesPresenter.getShareLocalization()) {
                Intent intentGeoService = new Intent(this, GeolocationUpdateService.class);
                stopService(intentGeoService);
            } else {
                startService(new Intent(this, GeolocationUpdateService.class));
            }
            EventBus.getDefault().post(new HotspotSettingsChanged(settingsPreferencesPresenter.getDriverType(),
                    settingsPreferencesPresenter.getShareLocalization()));
            mainPreferenceFragment.updateDriverTypeEnableBefore();
        } else if (preference.getKey().equals(Const.DRIVER_TYPE)) {
            showPopup(preference, false);
        }
    }

    private void showPopup(Preference preference, boolean shouldUncheckLocalization) {
        HotspotPopupHelper.showFirstPopup(this, settingsPreferencesPresenter.getDriverType(),
                selectedDriverType -> {
                    settingsPreferencesPresenter
                            .updateDriverTypeAndShareLocalisation(selectedDriverType.getName(), settingsPreferencesPresenter.getShareLocalization());
                    EventBus.getDefault().post(new HotspotSettingsChanged(selectedDriverType.getName(), true));
                },
                () -> {
                    if (shouldUncheckLocalization) {
                        ((SwitchPreference) preference).setChecked(false);
                        settingsPreferencesPresenter.updateShareLocalization(preference, false);
                        mainPreferenceFragment.updateDriverTypeEnableAfter();
                    }
                });
    }

    @SuppressLint("ValidFragment")
    public class MainPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            updateAbout();

            for (String setting : settings) {
                Preference preference = findPreference(setting);
                settingsPreferencesPresenter.bindPreferenceToValue(preference);

                switch (preference.getKey()){
                    case Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION:
                        SwitchPreference switchPreference = ((SwitchPreference)preference);
                        switchPreference.setChecked(settingsPreferencesPresenter.getShareLocalization());
                        mainPreferenceFragment.findPreference(Const.DRIVER_TYPE).setEnabled(switchPreference.isChecked());
                        break;

                    case Const.DRIVER_TYPE:
                        String v = settingsPreferencesPresenter.getDriverType();
                        ((ListPreference)preference).setValue(v);
                        break;
                }
            }
        }

        public void updateDriverTypeEnableBefore() {
            mainPreferenceFragment.findPreference(Const.DRIVER_TYPE)
                    .setEnabled(!settingsPreferencesPresenter.getShareLocalization());
        }

        public void updateDriverTypeEnableAfter() {
            mainPreferenceFragment.findPreference(Const.DRIVER_TYPE)
                    .setEnabled(settingsPreferencesPresenter.getShareLocalization());
        }

        private void updateAbout() {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
                findPreference("version").setSummary(String.format("Drively %s", packageInfo.versionName.split(" ")[0]));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
