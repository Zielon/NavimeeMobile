package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.contracts.repositories.FinanceRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Finance;

import java.util.Date;

import javax.inject.Inject;

public class FinanceRepositoryImpl<T extends Finance> implements FinanceRepository<T> {

    protected DataManager dataManager;

    private final String country;

    protected String baseFirestorePath;

    protected CollectionReference collectionReference;

    @Inject
    public FinanceRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
        this.country = dataManager.getPreferencesHelper().getCountry();
    }

    protected void setCollectionReference() {
        collectionReference = dataManager.getFirebaseService().getFirebaseFirestore()
                .collection(baseFirestorePath)
                .document(country)
                .collection(dataManager.getPreferencesHelper().getUserId());
    }

    @Override
    public Task<Void> save(T t) {
        DocumentReference newFinanceReference = collectionReference.document();
        t.setId(newFinanceReference.getId());
        return newFinanceReference.set(t.toMap());
    }

    @Override
    public Task<Void> remove(T t) {
        return collectionReference.document(t.getId()).delete();
    }

    @Override
    public Task<Void> edit(T t) {
        return null;
    }

    @Override
    public Task<QuerySnapshot> findAll() {
        return collectionReference.orderBy("date", Query.Direction.DESCENDING).get();
    }

    @Override
    public Task<QuerySnapshot> findAllByDateRange(Date from, Date to) {
        return null;
    }
}
