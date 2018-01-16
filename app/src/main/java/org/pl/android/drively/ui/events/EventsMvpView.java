package org.pl.android.drively.ui.events;

import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

/**
 * Created by Wojtek on 2017-10-21.
 */

public interface EventsMvpView extends MvpView {


    void showEventsEmpty();

    void showError();

    void onSuccessSave();

    void showEvents(List<Event> eventsList);
}
