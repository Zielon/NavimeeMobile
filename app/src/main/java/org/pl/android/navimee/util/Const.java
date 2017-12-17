package org.pl.android.navimee.util;

/**
 * Created by Wojtek on 2017-11-19.
 */

public class Const {
    public final static String MESSAGING_TOKEN = "MESSAGING_TOKEN";
    public final static String LAST_LOCATION = "LAST_LOCATION";
    public final static String LAST_LOCATION_LAT = "LAST_LOCATION_LAT";
    public final static String LAST_LOCATION_LNG = "LAST_LOCATION_LNG";

    public enum HotSpotType {
        EVENT,
        UBER_MULTIPLIER,
        FOURSQUARE_PLACE
    }
    public enum EventType {
        PREDICT_HQ,
        FACEBOOK
    }
}
