package org.pl.android.drively.ui.finance.pages.daily;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class DailyPresenter extends BaseFinancePresenter<DailyMvpView> {

    private DocumentSnapshot lastSnapshot;

    @Inject
    public DailyPresenter(ExpenseRepository expenseRepository,
                          CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        super(expenseRepository, compositeDisposable, schedulerProvider);
    }

    public void getMonthFinances(Calendar calendar) {
        getMvpView().showProgressDialog(R.string.loading_finances);
        expenseRepository.findAll().orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .whereGreaterThanOrEqualTo(DATE_FIELD, CalendarUtil.getFirstDayOfMonth(calendar))
                .whereLessThanOrEqualTo(DATE_FIELD, CalendarUtil.getLastDayOfMonth(calendar))
                .addSnapshotListener((documentSnapshots, error) -> {
                    if (error != null) {
                        getMvpView().showMessage(R.string.finances_load_failed);
                    } else {
                        List<Expense> expenses = documentSnapshots.toObjects(Expense.class);
                        mapExpensesIntoDailyFinance(expenses);
                        sumUpFinancesAndUpdateMainLabel(expenses);
                    }
                });
    }

    private void sumUpFinancesAndUpdateMainLabel(List<? extends Finance> finances) {
        getMvpView().setPanelAmount(DoubleUtil.getStringWithCurrencyFromDouble(
                Stream.of(finances).mapToDouble(Finance::getAmountWithoutCurrency).sum())
        );
    }

    private void mapExpensesIntoDailyFinance(List<? extends Finance> newFinances) {
        compositeDisposable.add(Observable.just(newFinances)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(finances -> {
                    Map<Date, List<Finance>> dailyFinances = StreamSupport.stream(finances)
                            .collect(Collectors.groupingBy(Finance::getDate));
                    Map<Date, List<? extends Finance>> parsedDailyFinances = new TreeMap<>(Collections.reverseOrder());
                    parsedDailyFinances.putAll(dailyFinances);
                    getMvpView().setData(parsedDailyFinances);
                    getMvpView().addAlreadyLoadedData(newFinances);
                    getMvpView().hideProgressDialog();
                })
        );
    }

    public void clearPaging() {
        lastSnapshot = null;
    }

}
