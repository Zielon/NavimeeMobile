package org.pl.android.drively.ui.planner;

import org.pl.android.drively.data.model.Event;

import java.util.Date;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class EventHelper {

    public static List<Event> sortEvents(List<Event> eventList) {
        Date today = new Date();
        today.setMinutes(today.getMinutes() + 30);
        return StreamSupport.stream(eventList)
                .sorted((event1, event2) -> Integer.valueOf(event1.getRank()).compareTo((event2.getRank())))
                .sorted((event1, event2) -> event1.getStartTime().compareTo(event2.getStartTime()))
                .collect(Collectors.toList());
    }

}
