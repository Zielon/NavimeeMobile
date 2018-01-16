package org.pl.android.drively.ui.signin;

import org.pl.android.drively.ui.base.MvpView;

public interface SignInMvpView extends MvpView {
    void onSuccess();

    void onError();
}
