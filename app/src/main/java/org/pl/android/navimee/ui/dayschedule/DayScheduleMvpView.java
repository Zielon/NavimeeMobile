package org.pl.android.navimee.ui.dayschedule;

import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.MvpView;

import java.util.List;
import java.util.Map;

/**
 * Created by Wojtek on 2017-10-30.
 */

public interface DayScheduleMvpView  extends MvpView {

    void showEvents(List<Event> events);

    void showEventsEmpty();

    void showError();

    void onSuccessDelete();
}
