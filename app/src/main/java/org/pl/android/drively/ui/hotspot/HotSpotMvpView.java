package org.pl.android.drively.ui.hotspot;

import org.pl.android.drively.data.model.CityNotAvailable;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.ui.base.tab.TabMvpView;

public interface HotSpotMvpView extends TabMvpView {

    void showEventOnMap(Event event);

    void showFoursquareOnMap(FourSquarePlace fourSquarePlace);

    void clusterMap();

    void showNotAvailableCity(CityNotAvailable city);
}
