package org.pl.android.drively.ui.finance.pages.monthly;

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

public class MonthlyPresenter extends BaseFinancePresenter<MonthlyMvpView> {

    @Inject
    public MonthlyPresenter(ExpenseRepository expenseRepository,
                            CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    @Override
    protected Object mapFinances(List<? extends Finance> finances, Date from, Date to) {
        Map<Integer, List<Finance>> dailyFinances = StreamSupport.stream(finances)
                .collect(Collectors.groupingBy(finance -> finance.getDate().getMonth()));
        Map<Integer, Double> monthlyFinances = StreamSupport.stream(dailyFinances.entrySet())
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        StreamSupport.stream(entry.getValue()).mapToDouble(Finance::getAmountWithoutCurrency).sum()));
        Map<Integer, Double> parsedMonthlyFinances = new TreeMap<>(Collections.reverseOrder());
        parsedMonthlyFinances.putAll(monthlyFinances);
        return parsedMonthlyFinances;
    }

    @Override
    protected void setData(Object object) {
        getMvpView().setData((Map<Integer, Double>) object);
    }

    public void getYearFinances(Calendar calendar) {
        getFinances(CalendarUtil.getFirstDayOfYear(calendar), CalendarUtil.getLastDayOfYear(calendar));
    }
}
