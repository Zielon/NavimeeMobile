package org.pl.android.drively.ui.settings.user.name;

import org.pl.android.drively.ui.base.MvpView;

public interface UserNameChangeMvpView extends MvpView {
    void onSuccess();

    void onError();
}