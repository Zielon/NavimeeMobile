package org.pl.android.drively.ui.settings.personalsettings;

import android.content.pm.PackageInfo;
import android.preference.Preference;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.Map;

import javax.inject.Inject;

import static org.pl.android.drively.util.ReflectionUtil.nameof;
import static org.pl.android.drively.util.ReflectionUtil.valueof;

public class SettingsPreferencesPresenter extends BasePresenter<SettingsPreferencesMvpView> {

    private final DataManager dataManager;
    private UsersRepository usersRepository;

    @Inject
    public SettingsPreferencesPresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.dataManager = dataManager;
        this.usersRepository = usersRepository;
    }

    public void updatePreference(Preference preference, Object newValue) {
        String userId = dataManager.getPreferencesHelper().getUID();
        try {
            usersRepository.updateUserField(userId, preference.getKey(), newValue);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setPreferences(Map<String, Preference> preferences) {
        String userId = dataManager.getPreferencesHelper().getUID();
        usersRepository.getUser(userId).addOnSuccessListener(user -> {
            try {
                for (Map.Entry<String, Preference> entry : preferences.entrySet()) {
                    Preference preference = preferences.get(nameof(User.class, entry.getKey()));
                    preference.setDefaultValue(valueof(user, entry.getKey()));
                    preference.setEnabled(true);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
