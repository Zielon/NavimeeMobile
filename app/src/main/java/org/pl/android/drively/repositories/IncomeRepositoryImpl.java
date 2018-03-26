package org.pl.android.drively.repositories;

import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

public class IncomeRepositoryImpl extends FinanceRepositoryImpl<Income> implements IncomeRepository {

    @Inject
    public IncomeRepositoryImpl(DataManager dataManager) {
        super(dataManager);
        this.baseFirestorePath = FirebasePaths.INCOMES;
        setCollectionReference();
    }

}
