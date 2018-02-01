package org.pl.android.drively.ui.signinup;

import org.pl.android.drively.ui.base.MvpView;

/**
 * Created by Wojtek on 2018-02-01.
 */

public interface BaseSignMvpView extends MvpView {
    void onSuccess();

    void onError(Throwable throwable);
}