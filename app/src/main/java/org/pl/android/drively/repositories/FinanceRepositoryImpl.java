package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import org.pl.android.drively.contracts.repositories.FinanceRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Finance;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.FINANCES;

public class FinanceRepositoryImpl implements FinanceRepository{

    private final DataManager dataManager;

    @Inject
    public FinanceRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public Task<DocumentReference> save(Finance finance) {
        return dataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FINANCES)
                .add(finance);
    }
}
