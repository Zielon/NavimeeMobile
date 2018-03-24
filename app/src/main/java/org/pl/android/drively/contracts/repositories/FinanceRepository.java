package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import org.pl.android.drively.data.model.Finance;

public interface FinanceRepository {
    Task<DocumentReference> save(Finance finance);
}
