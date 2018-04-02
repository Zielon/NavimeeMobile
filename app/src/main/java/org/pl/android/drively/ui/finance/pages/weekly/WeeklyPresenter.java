package org.pl.android.drively.ui.finance.pages.weekly;

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

public class WeeklyPresenter extends BaseFinancePresenter<WeeklyMvpView> {

    @Inject
    public WeeklyPresenter(ExpenseRepository expenseRepository,
                           CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    @Override
    protected Object mapFinances(List<? extends Finance> finances, Date from, Date to) {
        Map<String, List<Finance>> dailyFinances = StreamSupport.stream(finances)
                .collect(Collectors.groupingBy(finance -> CalendarUtil.determineWeekString(finance.getDate(), from, to)));
        Map<String, Double> monthlyFinances = StreamSupport.stream(dailyFinances.entrySet())
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        StreamSupport.stream(entry.getValue()).mapToDouble(Finance::getAmountWithoutCurrency).sum()));
        Map<String, Double> parsedMonthlyFinances = new TreeMap<>(Collections.reverseOrder());
        parsedMonthlyFinances.putAll(monthlyFinances);
        return parsedMonthlyFinances;
    }

    @Override
    protected void setData(Object object) {
        getMvpView().setData((Map<String, Double>) object);
    }

    public void getYearFinances(Calendar calendar) {
        Date firstDay = CalendarUtil.determineFirstDayForWeeklyUpdates(calendar);
        Date lastDay = CalendarUtil.determineLastDayForWeeklyUpdates(calendar);
        getFinances(firstDay, lastDay);
    }
}
