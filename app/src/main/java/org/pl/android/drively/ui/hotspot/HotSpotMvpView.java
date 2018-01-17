package org.pl.android.drively.ui.hotspot;

import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.model.FourSquarePlace;
import org.pl.android.drively.ui.base.MvpView;

/**
 * Created by Wojtek on 2017-10-28.
 */

public interface HotSpotMvpView extends MvpView {

    void showEventOnMap(Event event);

    void showFoursquareOnMap(FourSquarePlace fourSquarePlace);

    void clusterMap();

}
