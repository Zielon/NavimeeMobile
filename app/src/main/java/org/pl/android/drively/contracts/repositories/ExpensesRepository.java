package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.data.model.expenses.ExpenseBase;

public interface ExpensesRepository {
    <T extends ExpenseBase> Task<Void> addExpense(T expense);

    <T extends ExpenseBase> Task<Void> removeExpense(T expense);

    <T extends ExpenseBase> Task<Void> editExpense(T expense);
}
