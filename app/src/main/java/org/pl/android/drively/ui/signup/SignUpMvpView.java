package org.pl.android.drively.ui.signup;

import org.pl.android.drively.ui.base.MvpView;

public interface SignUpMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);
}
