package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.contracts.repositories.ExpensesRepository;
import org.pl.android.drively.data.model.expenses.ExpenseBase;


public class ExpensesRepositoryImpl implements ExpensesRepository {
    @Override
    public <T extends ExpenseBase> Task<Void> addExpense(T expense) {
        return null;
    }

    @Override
    public <T extends ExpenseBase> Task<Void> removeExpense(T expense) {
        return null;
    }

    @Override
    public <T extends ExpenseBase> Task<Void> editExpense(T expense) {
        return null;
    }
}
