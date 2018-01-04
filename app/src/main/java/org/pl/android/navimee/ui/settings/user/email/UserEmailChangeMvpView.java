package org.pl.android.navimee.ui.settings.user.email;

import org.pl.android.navimee.ui.base.MvpView;

public interface UserEmailChangeMvpView extends MvpView {
    void onSuccess();
    void onError(Throwable throwable);
}
