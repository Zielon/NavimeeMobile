package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.data.model.Finance;

import java.util.Date;

public interface FinanceRepository<T extends Finance> {
    Task<Void> save(T t);
    Task<Void> remove(T t);
    Task<Void> edit(T t);
    Task<QuerySnapshot> findAll();
    Task<QuerySnapshot> findAllByDateRange(Date from, Date to);
}
