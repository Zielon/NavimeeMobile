package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public interface CategoryRepository {
    Task<DocumentSnapshot> findAll();
}
