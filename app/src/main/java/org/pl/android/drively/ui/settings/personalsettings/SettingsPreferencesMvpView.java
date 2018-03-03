package org.pl.android.drively.ui.settings.personalsettings;

import android.preference.Preference;

import org.pl.android.drively.ui.base.MvpView;

interface SettingsPreferencesMvpView extends MvpView {
    void showAppropriatePopup(Preference preference);
}
