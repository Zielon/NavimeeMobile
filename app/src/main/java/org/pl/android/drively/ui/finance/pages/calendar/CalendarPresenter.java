package org.pl.android.drively.ui.finance.pages.calendar;

import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.pages.BaseFinancePresenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class CalendarPresenter extends BaseFinancePresenter<CalendarMvpView> {

    @Inject
    public CalendarPresenter(ExpenseRepository expenseRepository,
                             CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    @Override
    protected Object mapFinances(List<? extends Finance> finances, Date from, Date to) {
        return null;
    }

    @Override
    protected void setData(Object object) {

    }
}
