package org.pl.android.drively.ui.settings.personalsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.ReflectionUtil.valueof;

public class SettingsPreferencesPresenter extends BasePresenter<SettingsPreferencesMvpView> {

    private final DataManager dataManager;
    private UsersRepository usersRepository;
    private Preference.OnPreferenceChangeListener preferenceChangeListener;

    @Inject
    public SettingsPreferencesPresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.dataManager = dataManager;
        this.usersRepository = usersRepository;
        this.preferenceChangeListener = (preference, newValue) -> {
            updatePreferenceAndCheckShareLocalisation(preference, newValue);

            if ("false".equals(newValue.toString()) || "true".equals(newValue.toString()))
                dataManager.getPreferencesHelper().setValue(preference.getKey(), Boolean.parseBoolean(newValue.toString()));
            else if (newValue instanceof String)
                dataManager.getPreferencesHelper().setValue(preference.getKey(), (String) newValue);
            return true;
        };
    }

    @Override
    public void attachView(SettingsPreferencesMvpView mvpView) {
        super.attachView(mvpView);
    }

    public void updateSharedPreferences(List<String> settings, Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        User user = dataManager.getPreferencesHelper().getUserInfo();

        try {
            for (String setting : settings){
                Object value = valueof(user, setting);
                if ("false".equals(value.toString()) || "true".equals(value.toString()))
                    prefs.putBoolean(setting, valueof(user, setting));
                else if (value instanceof String)
                    prefs.putString(setting, valueof(user, setting));
            }
            prefs.apply();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void updatePreference(Preference preference, Object newValue) {
        String userId = dataManager.getPreferencesHelper().getUserId();
        try {
            usersRepository.updateUserField(userId, preference.getKey(), newValue);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void updatePreferenceAndCheckShareLocalisation(Preference preference, Object newValue) {
        updatePreference(preference, newValue);
        if (preference.getKey().equals(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION)) {
            if (getMvpView() != null) {
                getMvpView().showAppropriatePopup(preference);
            }
        }
    }

    public String getDriverType() {
        return dataManager.getPreferencesHelper().getValueString(Const.DRIVER_TYPE);
    }

    public void updateDriverTypeAndShareLocalisation(String driverType, boolean shareLocalisation) {
        dataManager.getPreferencesHelper().setValue(Const.DRIVER_TYPE, driverType);
        dataManager.getPreferencesHelper().setValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalisation);
        try {
            usersRepository.updateUserField(dataManager.getPreferencesHelper().getUserId(), "driverType", driverType);
            usersRepository.updateUserField(dataManager.getPreferencesHelper().getUserId(), Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalisation);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void bindPreferenceToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangeListener);
    }

    public void updateShareLocalization(Preference preference, boolean shareLocalization) {
        preference.getEditor().putBoolean(preference.getKey(), shareLocalization);
        try {
            dataManager.getPreferencesHelper().setValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalization);
            usersRepository.updateUserField(dataManager.getPreferencesHelper().getUserId(), Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION, shareLocalization);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public boolean getShareLocalization() {
        return dataManager.getPreferencesHelper().getValue(Const.SETTINGS_PREFERENCE_SHARE_LOCALIZATION);
    }
}
