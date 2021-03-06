package org.pl.android.drively.ui.finance.form.add;

import org.pl.android.drively.contracts.repositories.CategoryRepository;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.finance.form.BaseFinanceFormPresenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class AddFinancePresenter extends BaseFinanceFormPresenter<AddFinanceMvpView> {

    @Inject
    AddFinancePresenter(CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider,
                        IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
                        CategoryRepository categoryRepository, DataManager dataManager) {
        super(compositeDisposable, schedulerProvider, incomeRepository, expenseRepository, categoryRepository, dataManager);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.dispose();
    }
}
