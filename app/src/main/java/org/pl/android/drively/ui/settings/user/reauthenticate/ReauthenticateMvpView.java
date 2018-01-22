package org.pl.android.drively.ui.settings.user.reauthenticate;

import org.pl.android.drively.ui.base.MvpView;

public interface ReauthenticateMvpView extends MvpView {
    void onSuccess();

    void onError();
}
