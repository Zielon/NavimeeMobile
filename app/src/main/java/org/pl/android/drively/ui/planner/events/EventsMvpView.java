package org.pl.android.drively.ui.planner.events;

import org.joda.time.DateTime;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.MvpView;
import java.util.List;

public interface EventsMvpView extends MvpView {


    void showEventsEmpty();

    void showError();

    void onSuccessSave();

    void showEvents(List<Event> eventsList, DateTime dateTime);
}
