package org.pl.android.drively.ui.signinup;

import org.pl.android.drively.data.DataManager;

import javax.inject.Inject;

public class SignPresenter extends BaseSignPresenter {

    @Inject
    public SignPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }
}