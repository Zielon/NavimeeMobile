package org.pl.android.drively.util;

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

    public static int REQUEST_CODE_REGISTER = 2000;
    public static String STR_EXTRA_ACTION_LOGIN = "login";
    public static String STR_EXTRA_ACTION_RESET = "resetpass";
    public static String STR_EXTRA_ACTION = "action";
    public static String STR_EXTRA_USERNAME = "username";
    public static String STR_EXTRA_PASSWORD = "password";
    public static String STR_DEFAULT_AVATAR = "DEFAULT";
    public static String UID = "";
    //TODO only use this UID for debug mode
//    public static String UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22";
    public static String INTENT_KEY_CHAT_FRIEND = "friendname";
    public static String INTENT_KEY_CHAT_AVATA = "friendavata";
    public static String INTENT_KEY_CHAT_ID = "friendid";
    public static String INTENT_KEY_CHAT_ROOM_ID = "roomid";
    public static long TIME_TO_REFRESH = 10 * 1000;
    public static long TIME_TO_OFFLINE = 2 * 60 * 1000;
    public static long FIVE_MEGABYTE = 5 * 1024 * 1024;

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
