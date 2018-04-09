package org.pl.android.drively.repositories;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.pl.android.drively.contracts.repositories.FinanceRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.util.BitmapUtils;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class FinanceRepositoryImpl<T extends Finance> implements FinanceRepository<T> {

    protected final DataManager dataManager;

    protected final SchedulerProvider schedulerProvider;

    private final String country;

    protected String baseFirestorePath;

    protected CollectionReference collectionReference;

    @Inject
    public FinanceRepositoryImpl(DataManager dataManager, SchedulerProvider schedulerProvider) {
        this.dataManager = dataManager;
        this.schedulerProvider = schedulerProvider;
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
    public Disposable saveWithBitmap(T t, final Bitmap bitmap, SuccessCallback successCallback, FailureCallback failureCallback) {
        DocumentReference financeReference;
        if (t.getId() != null) {
            financeReference = collectionReference.document(t.getId());
        } else {
            financeReference = collectionReference.document();
            t.setId(financeReference.getId());
        }
        t.setAttachmentPath(buildPathByFinance(t.getId()));
        return Observable.just(t)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(finance -> {
                            FirebaseStorage.getInstance().getReference()
                                    .child(finance.getAttachmentPath())
                                    .putBytes(BitmapUtils.parseBitmapIntoBytes(bitmap));
                            financeReference.set(finance)
                                    .addOnCompleteListener(task -> successCallback.onSuccess());
                        },
                        error -> failureCallback.onFailure()
                );
    }

    @Override
    public String buildPathByFinance(String id) {
        return baseFirestorePath + "/"
                + dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid() + "/" + id;
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
    public CollectionReference findAll() {
        return collectionReference;
    }

    @Override
    public Task<QuerySnapshot> findAllByDateRange(Date from, Date to) {
        return null;
    }

    @FunctionalInterface
    public interface SuccessCallback {
        void onSuccess();
    }

    @FunctionalInterface
    public interface FailureCallback {
        void onFailure();
    }
}
