package org.pl.android.drively.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.drively.contracts.repositories.CategoryRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

public class CategoryRepositoryImpl implements CategoryRepository {

    private final DataManager dataManager;

    private String country;

    @Inject
    public CategoryRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
        this.country = dataManager.getPreferencesHelper().getCountry();
    }

    @Override
    public Task<DocumentSnapshot> findAll() {
        return dataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FirebasePaths.EXPENSES)
                .document(country)
                .get();
    }
}
