package org.pl.android.drively.ui.dayschedule;

import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

/**
 * Created by Wojtek on 2017-10-30.
 */

public interface DayScheduleMvpView  extends MvpView {

    void showEvents(List<Event> events);

    void showEventsEmpty();

    void showError();

    void onSuccessDelete(Event event);
}
