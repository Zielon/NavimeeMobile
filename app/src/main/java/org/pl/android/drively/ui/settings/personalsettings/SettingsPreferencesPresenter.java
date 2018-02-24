package org.pl.android.drively.ui.settings.personalsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

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
            updatePreference(preference, newValue);

            if (Boolean.valueOf(newValue.toString()) != null)
                dataManager.getPreferencesHelper().setValue(preference.getKey(), Boolean.parseBoolean(newValue.toString()));
            else if (newValue instanceof String)
                dataManager.getPreferencesHelper().setValue(preference.getKey(), (String) newValue);
            return true;
        };
    }

    public void updateSharedPreferences(List<String> settings, Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        User user = dataManager.getPreferencesHelper().getUserInfo();

        try {
            for (String setting : settings)
                prefs.putBoolean(setting, valueof(user, setting));
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

    public void bindPreferenceToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangeListener);
    }
}
