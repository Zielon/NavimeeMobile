package org.pl.android.drively.repositories;

import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;


public class ExpensesRepositoryImpl extends FinanceRepositoryImpl<Expense> implements ExpenseRepository {

    @Inject
    public ExpensesRepositoryImpl(DataManager dataManager) {
        super(dataManager);
        this.baseFirestorePath = FirebasePaths.EXPENSES;
        setCollectionReference();
    }

}
