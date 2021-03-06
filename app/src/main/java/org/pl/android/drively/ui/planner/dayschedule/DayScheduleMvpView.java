package org.pl.android.drively.ui.planner.dayschedule;

import org.joda.time.DateTime;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

public interface DayScheduleMvpView extends MvpView {

    void showEvents(List<Event> events, DateTime dateTime);

    void showEventsEmpty();

    void showError();

    void onSuccessDelete(Event event);
}
