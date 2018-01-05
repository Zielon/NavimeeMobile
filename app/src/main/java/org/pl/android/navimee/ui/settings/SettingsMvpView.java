package org.pl.android.navimee.ui.settings;

import org.pl.android.navimee.ui.base.MvpView;

public interface SettingsMvpView extends MvpView {
    void onSuccess();

    void onLogout();

    void onError();
}
