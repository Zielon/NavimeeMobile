package org.pl.android.drively.ui.settings.user.email;

import org.pl.android.drively.ui.base.MvpView;

public interface UserEmailChangeMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);
}
