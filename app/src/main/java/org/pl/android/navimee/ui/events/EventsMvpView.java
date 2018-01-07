package org.pl.android.navimee.ui.events;

import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.MvpView;

import java.util.List;
import java.util.Map;

/**
 * Created by Wojtek on 2017-10-21.
 */

public interface EventsMvpView extends MvpView {


    void showEventsEmpty();

    void showError();

    void onSuccessSave();

    void showEvents(List<Event> eventsList);
}
