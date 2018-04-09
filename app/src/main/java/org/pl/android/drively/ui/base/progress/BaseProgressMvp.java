package org.pl.android.drively.ui.base.progress;

import org.pl.android.drively.ui.base.MvpView;

public interface BaseProgressMvp extends MvpView {

    void showProgressDialog(String content);

    void showProgressDialog(int stringResId);

    void hideProgressDialog();

    void showMessage(String content);

    void showMessage(int stringResId);

}
