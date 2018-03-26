package org.pl.android.drively.ui.finance;

import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.ui.base.tab.BaseTabPresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class FinancePresenter extends BaseTabPresenter<FinanceMvpView> {

    private final ExpenseRepository expenseRepository;

    private final IncomeRepository incomeRepository;

    private Disposable mDisposable;

    @Inject
    public FinancePresenter(DataManager dataManager, IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        mDataManager = dataManager;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void attachView(FinanceMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void loadFinances() {
        //TODO: load finances from firestore
        expenseRepository.findAll().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Expense> expenses = task.getResult().toObjects(Expense.class);
                if (expenses.isEmpty()) {
                    getMvpView().showNoDataLabel();
                } else {
                    getMvpView().updateFinances(expenses);
                }
            } else {
                Timber.d(task.getException());
                getMvpView().showNoDataLabel();
            }
        });
    }
}
