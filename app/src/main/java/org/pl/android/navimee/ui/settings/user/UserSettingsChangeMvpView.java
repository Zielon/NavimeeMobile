package org.pl.android.navimee.ui.settings.user;

import org.pl.android.navimee.ui.base.MvpView;

public interface UserSettingsChangeMvpView extends MvpView {
    void onSuccess();
    void onError();
}
