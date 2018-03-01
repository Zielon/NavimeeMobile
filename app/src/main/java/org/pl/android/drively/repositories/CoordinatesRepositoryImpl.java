package org.pl.android.drively.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.drively.contracts.repositories.CoordinatesRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.City;
import org.pl.android.drively.data.model.CityNotAvailable;
import org.pl.android.drively.util.FirebasePaths;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.CITIES;

public class CoordinatesRepositoryImpl implements CoordinatesRepository{

    private DataManager dataManager;
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    public CoordinatesRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public Task<List<City>> getAvailableCities(String country) {
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(FirebasePaths.AVAILABLE_CITIES)
                .document(country.toUpperCase())
                .collection(CITIES).get().continueWith(cities -> {
                    List<City> friendsIds = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : cities.getResult().getDocuments())
                        friendsIds.add(mapper.convertValue(documentSnapshot.getData(), City.class));
                    return friendsIds;
                });
    }

    @Override
    public Task<Task<Void>> updateUnavailableCity(CityNotAvailable city) {
        DocumentReference cityRef = dataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FirebasePaths.NOT_AVAILABLE_CITIES)
                .document(city.getCity().toUpperCase());

        return cityRef.get().continueWith(missingCity ->{
            DocumentSnapshot snapshot = missingCity.getResult();
            if(snapshot.exists()){
                CityNotAvailable cityNotAvailable = snapshot.toObject(CityNotAvailable.class);
                return cityRef.update("count", cityNotAvailable.getCount() + 1);
            }else
                return cityRef.set(city);
        });
    }
}
