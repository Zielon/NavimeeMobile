package org.pl.android.drively.ui.finance.pages;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.Query;

import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.base.Presenter;
import org.pl.android.drively.util.DoubleUtil;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import java8.util.Optional;
import timber.log.Timber;

public abstract class BaseFinancePresenter<V extends BaseFinanceMvp> extends BasePresenter<V> implements Presenter<V> {

    protected static final String DATE_FIELD = "date";

    protected final ExpenseRepository expenseRepository;

    protected final CompositeDisposable compositeDisposable;

    protected final SchedulerProvider schedulerProvider;

    public BaseFinancePresenter(ExpenseRepository expenseRepository,
                                CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        this.expenseRepository = expenseRepository;
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
    }

    protected abstract Object mapFinances(List<? extends Finance> finances, Date from, Date to);

    protected abstract void setData(Object object);

    public void getFinances(Date from, Date to) {
        expenseRepository.findAll().orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .whereGreaterThanOrEqualTo(DATE_FIELD, from)
                .whereLessThanOrEqualTo(DATE_FIELD, to)
                .addSnapshotListener((documentSnapshots, error) -> {
                    if (error != null) {
                        getMvpView().showMessage(R.string.finances_load_failed);
                    } else {
                        List<Expense> expenses = documentSnapshots.toObjects(Expense.class);
                        mapFinancesAsynchronously(expenses, from, to);
                        sumUpFinancesAndUpdateMainLabel(expenses);
                    }
                });
    }

    private void sumUpFinancesAndUpdateMainLabel(List<? extends Finance> finances) {
        getMvpView().setPanelAmount(DoubleUtil.getStringWithCurrencyFromDouble(
                Stream.of(finances).mapToDouble(Finance::getAmountWithoutCurrency).sum())
        );
    }

    private void mapFinancesAsynchronously(List<? extends Finance> newFinances, Date from, Date to) {
        compositeDisposable.add(Observable.just(newFinances)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(finances -> {
                    Object object = mapFinances(finances, from, to);
                    try {
                        setData(object);
                    } catch (ClassCastException e) {
                        Timber.d("Mapping finances failed due to: " + e.getMessage());
                    }
                    getMvpView().addAlreadyLoadedData(newFinances);
                    getMvpView().hideProgressDialog();
                }, error -> Optional.ofNullable(getMvpView()).ifPresent(mvpView -> mvpView.showMessage(R.string.something_went_wrong)))
        );
    }

    @Override
    public void attachView(V mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        compositeDisposable.dispose();
        super.detachView();
    }

}
