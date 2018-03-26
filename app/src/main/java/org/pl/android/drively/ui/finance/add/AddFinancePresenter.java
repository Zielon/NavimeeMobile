package org.pl.android.drively.ui.finance.add;


import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.CategoryRepository;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import lombok.Getter;

public class AddFinancePresenter extends BasePresenter<AddFinanceMvpView> {

    private final IncomeRepository incomeRepository;

    private final ExpenseRepository expenseRepository;

    private final CategoryRepository categoryRepository;

    private CompositeDisposable compositeDisposable;

    private SchedulerProvider schedulerProvider;

    @Getter
    private List<String> categories = new ArrayList<>();

    @Inject
    AddFinancePresenter(CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider,
                        IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
                        CategoryRepository categoryRepository) {
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    void saveExpense(Expense expense) {
        expenseRepository.save(expense).addOnCompleteListener(documentReference -> {
            getMvpView().hideProgressDialog();
            if (documentReference.isSuccessful()) {
                getMvpView().finishActivity();
            } else {
                getMvpView().showMessage(R.string.finance_failed_to_add);
            }
        });
    }

    void saveIncome(Income income) {
        incomeRepository.save(income).addOnCompleteListener(documentReference -> {
            getMvpView().hideProgressDialog();
            if (documentReference.isSuccessful()) {
                getMvpView().finishActivity();
            } else {
                getMvpView().showMessage(R.string.finance_failed_to_add);
            }
        });
    }

    void loadCategories() {
        categoryRepository.findAll().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categories = (List<String>) task.getResult().getData().get("categories");
            }
        });
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.dispose();
    }
}
