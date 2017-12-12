package org.pl.android.navimee.ui.hotspot;

import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.data.model.FourSquarePlace;
import org.pl.android.navimee.ui.base.MvpView;

/**
 * Created by Wojtek on 2017-10-28.
 */

public interface HotSpotMvpView  extends MvpView {

    void  showEventOnMap(Event event);
    void  showFoursquareOnMap(FourSquarePlace fourSquarePlace);

}
