package org.pl.android.drively.contracts.repositories;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.repositories.FinanceRepositoryImpl;

import java.util.Date;

import io.reactivex.disposables.Disposable;

public interface FinanceRepository<T extends Finance> {
    Task<Void> save(T t);

    Disposable saveWithBitmap(T t, final Bitmap bitmap,
                              FinanceRepositoryImpl.SuccessCallback successCallback, FinanceRepositoryImpl.FailureCallback failureCallback);

    Task<Void> remove(String id);

    CollectionReference findAll();

    Task<QuerySnapshot> findAllByDateRange(Date from, Date to);

    String buildPathByFinance(String id);
}
