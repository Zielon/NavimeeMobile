package org.pl.android.drively.ui.planner;

import org.pl.android.drively.data.model.Event;

import java.util.Date;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class EventHelper {

    public static List<Event> filterAndSortEvents(List<Event> eventList) {
        return StreamSupport.stream(eventList)
                .filter(event -> event.getEndTime().compareTo(new Date()) > 0)
                .sorted((event1, event2) -> Integer.valueOf(event1.getRank()).compareTo((event2.getRank())))
                .sorted((event1, event2) -> event1.getStartTime().compareTo(event2.getStartTime()))
                .collect(Collectors.toList());
    }

}
