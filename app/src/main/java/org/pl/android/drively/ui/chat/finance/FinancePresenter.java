package org.pl.android.drively.ui.chat.finance;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.base.tab.BaseTabPresenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class FinancePresenter extends BaseTabPresenter<FinanceMvpView>{

    private Disposable mDisposable;

    @Inject
    public FinancePresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(FinanceMvpView mvpView) {super.attachView(mvpView); }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

}
