package org.pl.android.drively.util;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.chat.ChatFragment;
import org.pl.android.drively.ui.chat.finance.FinanceFragment;
import org.pl.android.drively.ui.hotspot.HotSpotFragment;
import org.pl.android.drively.ui.planner.events.EventsFragment;

import java.util.Arrays;
import java.util.List;

import java8.util.stream.StreamSupport;

public class Const {
    public final static String ADMIN = "ADMIN_DRIVELY";
    public final static String MESSAGING_TOKEN = "MESSAGING_TOKEN";
    public final static String LAST_LOCATION = "LAST_LOCATION";
    public final static String LAST_LOCATION_LAT = "LAST_LOCATION_LAT";
    public final static String LAST_LOCATION_LNG = "LAST_LOCATION_LNG";
    public final static String IS_FEEDBACK = "IS_FEEDBACK";
    public final static String LOCATION_NAME = "LOCATION_NAME";
    public final static String LOCATION_ADDRESS = "LOCATION_ADDRESS";
    public final static String FEEDBACK_ID = "FEEDBACK_ID";
    public final static String USER_COMPANY = "USER_COMPANY";
    public final static String NAME = "NAME";
    public final static String FIRST_START = "firstStart";

    public final static String FIRST_START_POPUP_SUFFIX = "FIRST_START_POPUP";
    public final static List<String> TAB_FRAGMENTS = Arrays.asList(HotSpotFragment.class.getSimpleName(),
            EventsFragment.class.getSimpleName(), ChatFragment.class.getSimpleName(), FinanceFragment.class.getSimpleName());
    public final static String HOTSPOT_SECOND_POPUP_FIRST_START = "HOTSPOT_SECOND_POPUP_" + FIRST_START_POPUP_SUFFIX;

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
    public static String INTENT_KEY_CHAT_ROOM_NAME = "roomname";
    public static String INTENT_KEY_IS_GROUP_CHAT = "isChatGroup";
    public static long TIME_TO_REFRESH = 10 * 1000;
    public static long TIME_TO_OFFLINE = 2 * 60 * 1000;
    public static long FIVE_MEGABYTE = 5 * 1024 * 1024;

    public static String SETTINGS_PREFERENCE_SHARE_LOCALISATION = "shareLocalization";
    public static String SETTINGS_PREFERENCE_CHAT_PRIVATE_NOTIFICATION = "chatPrivateNotification";
    public static String SETTINGS_PREFERENCE_GROUP_NOTIFICATION = "chatGroupNotification";
    public static String SETTINGS_PREFERENCE_DAY_SCHEDULE_NOTIFICATION = "dayScheduleNotification";

    public enum HotSpotType {
        EVENT,
        UBER_MULTIPLIER,
        FOURSQUARE_PLACE,
        USER_LOCATION
    }

    public enum EventType {
        PREDICT_HQ,
        FACEBOOK
    }

    public enum NotificationsType {
        FEEDBACK,
        SCHEDULED_EVENT,
        BIG_EVENT,
        MESSAGE_PRIVATE,
        MESSAGE_GROUP
    }

    public enum DriverType {
        UBER(R.id.uber, "Uber"),
        MY_TAXI(R.id.myTaxi, "myTaxi"),
        ITAXI(R.id.iTaxi, "iTaxi"),
        TAXI(R.id.Taxi, "Taxi");

        int buttonResId;

        String name;

        DriverType(int buttonResId, String name) {
            this.buttonResId = buttonResId;
            this.name = name;
        }

        public static DriverType getByName(String name) {
            return StreamSupport.stream(Arrays.asList(Const.DriverType.values()))
                    .filter(driverTypeInner -> driverTypeInner.getName().equals(name))
                    .findFirst()
                    .get();
        }

        public int getButtonResId() {
            return buttonResId;
        }

        public String getName() {
            return name;
        }
    }

}
