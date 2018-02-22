package org.pl.android.drively.ui.settings.personalsettings;

import android.preference.Preference;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.FirebasePaths;

import java.util.Map;

import javax.inject.Inject;

import static org.pl.android.drively.util.ReflectionUtil.nameof;
import static org.pl.android.drively.util.ReflectionUtil.valueof;

@ConfigPersistent
public class SettingsPreferencesPresenter extends BasePresenter<SettingsPreferencesMvpView> {

    private final DataManager dataManager;

    @Inject
    public SettingsPreferencesPresenter(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void updatePreference(Preference preference, Object newValue){
        String userId = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public void setDefaultPreferences(Map<String, Preference> preferences) {

        String userId = dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.USERS).document(userId)
                .addSnapshotListener((document, e) -> {
                    User user = document.toObject(User.class);
                    try {
                        for (Map.Entry<String, Preference> entry : preferences.entrySet()) {
                            {
                                Preference preference = preferences.get(nameof(User.class, entry.getKey()));
                                preference.setDefaultValue(valueof(user, entry.getKey()));
                                preference.setEnabled(true);
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }
}
