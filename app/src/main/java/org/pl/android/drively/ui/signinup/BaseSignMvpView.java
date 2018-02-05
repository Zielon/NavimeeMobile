package org.pl.android.drively.ui.signinup;

import org.pl.android.drively.ui.base.MvpView;

public interface BaseSignMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);
}