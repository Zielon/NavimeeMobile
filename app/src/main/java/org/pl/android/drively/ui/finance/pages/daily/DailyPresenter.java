package org.pl.android.drively.ui.finance.pages.daily;

import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.pages.BaseFinancePresenter;
import org.pl.android.drively.util.CalendarUtil;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class DailyPresenter extends BaseFinancePresenter<DailyMvpView> {

    @Inject
    public DailyPresenter(ExpenseRepository expenseRepository,
                          CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    public void getMonthFinances(Calendar calendar) {
        getFinances(CalendarUtil.getFirstDayOfMonth(calendar), CalendarUtil.getLastDayOfMonth(calendar));
    }

    @Override
    protected void setData(Object object) {
        getMvpView().setData((Map<Date, List<? extends Finance>>) object);
    }

    protected Object mapFinances(List<? extends Finance> finances, Date from, Date to) {
        Map<Date, List<Finance>> dailyFinances = StreamSupport.stream(finances)
                .collect(Collectors.groupingBy(Finance::getDate));
        Map<Date, List<? extends Finance>> parsedDailyFinances = new TreeMap<>(Collections.reverseOrder());
        parsedDailyFinances.putAll(dailyFinances);
        return parsedDailyFinances;
    }

}
