package org.pl.android.navimee.util;

/**
 * Created by Wojtek on 2017-11-19.
 */

public class Const {
    public final static String MESSAGING_TOKEN = "MESSAGING_TOKEN";
    public final static String LAST_LOCATION = "LAST_LOCATION";
    public final static String LAST_LOCATION_LAT = "LAST_LOCATION_LAT";
    public final static String LAST_LOCATION_LNG = "LAST_LOCATION_LNG";
    public final static String IS_FEEDBACK = "IS_FEEDBACK";
    public final static String LOCATION_NAME = "LOCATION_NAME";
    public final static String LOCATION_ADDRESS = "LOCATION_ADDRESS";
    public final static String FEEDBACK_ID = "FEEDBACK_ID";
    public final static String NAME = "NAME";

    public enum HotSpotType {
        EVENT,
        UBER_MULTIPLIER,
        FOURSQUARE_PLACE
    }
    public enum EventType {
        PREDICT_HQ,
        FACEBOOK
    }

    public enum NotificationsType {
        FEEDBACK,
        SCHEDULED_EVENT,
        BIG_EVENT
    }

}
