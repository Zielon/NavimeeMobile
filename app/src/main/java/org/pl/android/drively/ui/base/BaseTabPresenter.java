package org.pl.android.drively.ui.base;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.util.Const;

public class BaseTabPresenter<T extends TabMvpView> extends BasePresenter<T>  {

    protected DataManager mDataManager;

    @Override
    public void attachView(T mvpView) {
        super.attachView(mvpView);
        verifyFirstStartPopupNecessity();
    }

    private void verifyFirstStartPopupNecessity() {
        mDataManager.getPreferencesHelper().clear();
        String sharedPreferenceConst = getMvpView().getClass().getSimpleName() + Const.FIRST_START_POPUP_SUFFIX;
        boolean wasFirstStartPopup = mDataManager.getPreferencesHelper()
                .getValue(sharedPreferenceConst);
        if(!wasFirstStartPopup) {
            getMvpView().showInstructionPopup();
            mDataManager.getPreferencesHelper().setValue(sharedPreferenceConst, true);
        }
    }

}
