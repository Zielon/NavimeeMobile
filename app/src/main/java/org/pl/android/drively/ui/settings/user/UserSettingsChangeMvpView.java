package org.pl.android.drively.ui.settings.user;

import android.graphics.Bitmap;

import org.pl.android.drively.ui.base.MvpView;

public interface UserSettingsChangeMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);

    void onUserDeleted();

    void reloadAvatar(Bitmap bitmap);
}
