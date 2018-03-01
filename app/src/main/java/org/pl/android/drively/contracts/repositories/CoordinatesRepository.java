package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.data.model.City;
import org.pl.android.drively.data.model.CityNotAvailable;

import java.util.List;

public interface CoordinatesRepository {
    Task<List<City>> getAvailableCities(String country);

    Task<Task<Void>> updateUnavailableCity(CityNotAvailable city);
}
