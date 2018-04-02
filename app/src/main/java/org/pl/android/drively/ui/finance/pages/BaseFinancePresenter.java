package org.pl.android.drively.ui.finance.pages;

import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.base.Presenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class BaseFinancePresenter<V extends BaseFinanceMvp> extends BasePresenter<V> implements Presenter<V> {

    protected static final int BATCH_QUANTITY = 30;

    protected static final String DATE_FIELD = "date";

    protected final ExpenseRepository expenseRepository;

    protected final CompositeDisposable compositeDisposable;

    protected final SchedulerProvider schedulerProvider;

    @Inject
    public BaseFinancePresenter(ExpenseRepository expenseRepository,
                                CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        this.expenseRepository = expenseRepository;
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void attachView(V mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.dispose();
    }

}
