package org.pl.android.drively.ui.finance.form;

import android.graphics.Bitmap;

import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.CategoryRepository;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import lombok.Getter;

public abstract class BaseFinanceFormPresenter<M extends BaseFinanceFormMvp> extends BasePresenter<M>{

    protected final IncomeRepository incomeRepository;

    protected final ExpenseRepository expenseRepository;

    protected final CategoryRepository categoryRepository;

    protected CompositeDisposable compositeDisposable;

    protected SchedulerProvider schedulerProvider;

    protected DataManager dataManager;

    @Getter
    protected List<String> categories = new ArrayList<>();

    public BaseFinanceFormPresenter(CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider,
                                    IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
                                    CategoryRepository categoryRepository, DataManager dataManager) {
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.dataManager = dataManager;
    }

    void saveExpenseWithBitmap(Expense expense, Bitmap bitmap) {
        if (bitmap != null) {
            compositeDisposable.add(expenseRepository.saveWithBitmap(expense, bitmap,
                    () -> {
                        getMvpView().hideProgressDialog();
                        getMvpView().finishActivity();
                    },
                    () -> getMvpView().showMessage(R.string.finance_failed_to_add)));
        } else {
            saveExpense(expense);
        }
    }

    private void saveExpense(Expense expense) {
        expenseRepository.save(expense).addOnCompleteListener(documentReference -> {
            getMvpView().hideProgressDialog();
            if (documentReference.isSuccessful()) {
                getMvpView().finishActivity();
            } else {
                getMvpView().showMessage(R.string.finance_failed_to_add);
            }
        });
    }

    void saveIncomeWithBitmap(Income income, Bitmap bitmap) {
        if (bitmap != null) {
            compositeDisposable.add(incomeRepository.saveWithBitmap(income, bitmap,
                    () -> {
                        getMvpView().hideProgressDialog();
                        getMvpView().finishActivity();
                    },
                    () -> getMvpView().showMessage(R.string.finance_failed_to_add)));
        } else {
            saveIncome(income);
        }
    }

    private void saveIncome(Income income) {
        incomeRepository.save(income).addOnCompleteListener(documentReference -> {
            getMvpView().hideProgressDialog();
            if (documentReference.isSuccessful()) {
                getMvpView().finishActivity();
            } else {
                getMvpView().showMessage(R.string.finance_failed_to_add);
            }
        });
    }

    public void loadCategories() {
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
