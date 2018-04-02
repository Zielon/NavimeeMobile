package org.pl.android.drively.ui.finance.pages.monthly;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.Query;

import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.pages.BaseFinancePresenter;
import org.pl.android.drively.util.CalendarUtil;
import org.pl.android.drively.util.DoubleUtil;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MonthlyPresenter extends BaseFinancePresenter<MonthlyMvpView> {

    @Inject
    public MonthlyPresenter(ExpenseRepository expenseRepository,
                            CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    public void getYearFinances(Calendar calendar) {
        getMvpView().showProgressDialog(R.string.loading_finances);
        expenseRepository.findAll().orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .whereGreaterThan(DATE_FIELD, CalendarUtil.getFirstDayOfYear(calendar))
                .whereLessThanOrEqualTo(DATE_FIELD, CalendarUtil.getLastDayOfYear(calendar))
                .addSnapshotListener((documentSnapshots, error) -> {
                    if (error != null) {
                        getMvpView().showMessage(R.string.finances_load_failed);
                    } else {
                        List<Expense> expenses = documentSnapshots.toObjects(Expense.class);
                        mapExpensesIntoMonthlyFinance(expenses);
                        sumUpFinancesAndUpdateMainLabel(expenses);
                    }
                });
    }

    private void sumUpFinancesAndUpdateMainLabel(List<? extends Finance> finances) {
        getMvpView().setPanelAmount(DoubleUtil.getStringWithCurrencyFromDouble(
                Stream.of(finances).mapToDouble(Finance::getAmountWithoutCurrency).sum())
        );
    }

    private void mapExpensesIntoMonthlyFinance(List<? extends Finance> newFinances) {
        compositeDisposable.add(Observable.just(newFinances)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(finances -> {
                    Map<Integer, List<Finance>> dailyFinances = StreamSupport.stream(finances)
                            .collect(Collectors.groupingBy(finance -> finance.getDate().getMonth()));
                    Map<Integer, Double> monthlyFinances = StreamSupport.stream(dailyFinances.entrySet())
                            .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                                    StreamSupport.stream(entry.getValue()).mapToDouble(Finance::getAmountWithoutCurrency).sum()));
                    Map<Integer, Double> parsedMonthlyFinances = new TreeMap<>(Collections.reverseOrder());
                    parsedMonthlyFinances.putAll(monthlyFinances);
                    getMvpView().setData(parsedMonthlyFinances);
                    getMvpView().addAlreadyLoadedData(newFinances);
                    getMvpView().hideProgressDialog();
                })
        );
    }
}
