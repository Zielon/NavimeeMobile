package org.pl.android.navimee.ui.settings;

import org.pl.android.navimee.ui.base.MvpView;

/**
 * Created by Wojtek on 2017-11-20.
 */

public interface SettingsMvpView extends MvpView {
    void onSuccess();

    void onLogout();
    void onError();
}
