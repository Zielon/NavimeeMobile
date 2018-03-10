package org.pl.android.drively.ui.base.tab;

import com.google.firebase.auth.FirebaseAuth;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;

public class BaseTabPresenter<T extends TabMvpView> extends BasePresenter<T> {

    protected DataManager mDataManager;

    @Override
    public void attachView(T mvpView) {
        super.attachView(mvpView);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            verifyFirstStartPopupNecessity();
        }
    }

    private void verifyFirstStartPopupNecessity() {
        String sharedPreferenceConst = getMvpView().getClass().getSimpleName() + Const.FIRST_START_POPUP_SUFFIX;
        boolean needFirstStartPopup = mDataManager.getPreferencesHelper().getValue(sharedPreferenceConst);
        if (needFirstStartPopup) {
            getMvpView().showInstructionPopup();
            if (!getMvpView().getClass().getSimpleName().equals("FinanceFragment")) {
                mDataManager.getPreferencesHelper().setValue(sharedPreferenceConst, false);
            }
        }
    }
}