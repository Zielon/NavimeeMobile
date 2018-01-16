package org.pl.android.drively.ui.settings;

import org.pl.android.drively.ui.base.MvpView;

public interface SettingsMvpView extends MvpView {
    void onSuccess();

    void onLogout();

    void onError();
}
