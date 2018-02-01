package org.pl.android.drively.ui.signinup.in;

import org.pl.android.drively.ui.base.MvpView;

public interface SignInMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);
}
