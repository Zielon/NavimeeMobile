package org.pl.android.navimee.ui.signin;

import org.pl.android.navimee.ui.base.MvpView;

public interface SignInMvpView extends MvpView {
    void onSuccess();
    void onError();
}
