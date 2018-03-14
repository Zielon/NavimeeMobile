package org.pl.android.drively.ui.planner;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;

import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class EventHelper {

    public static List<Event> sortEvents(List<Event> eventList) {
        return StreamSupport.stream(eventList)
                .sorted((event1, event2) -> Integer.valueOf(event2.getRank()).compareTo((event1.getRank())))
                .sorted((event1, event2) -> event1.getStartTime().compareTo(event2.getStartTime()))
                .collect(Collectors.toList());
    }

    public static int getIconResIdByRank(int rank) {
        switch (rank) {
            case 1:
                return R.mipmap.ranking_1;
            case 2:
                return R.mipmap.ranking_2;
            case 3:
                return R.mipmap.ranking_3;
            case 4:
                return R.mipmap.ranking_4;
            case 5:
                return R.mipmap.ranking_5;
            default:
                return R.mipmap.ranking_1;
        }
    }

    public static int convertRatingIntoRank(double rating) {
        int rank = (int) Math.round(rating / 2);
        if (rank == 0) {
            return 1;
        }
        return rank;
    }

}
