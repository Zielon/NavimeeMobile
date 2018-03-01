package org.pl.android.drively.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.drively.data.model.User;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;
import javax.inject.Singleton;

import java8.util.stream.StreamSupport;

import static org.pl.android.drively.util.ReflectionUtil.nameof;

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "android_boilerplate_pref_file";
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_AVATAR = "avatar";
    private static String SHARE_KEY_USER_ID = "id";
    private static String APP_VERSION = "app_version";
    private static String USER_COUNTRY = "user_country";
    private static String USER_CITY = "user_city";
    private static String IS_ONLINE = "is_online";

    private static String DAY_SCHEDULE_NOTIFICATION;
    private static String BIG_EVENTS_NOTIFICATION;
    private static String CHAT_PRIVATE_NOTIFICATION;
    private static String CHAT_GROUP_NOTIFICATION;
    private static String SHARE_LOCALIZATION;

    private static ListenerRegistration userListenerRegistration;
    private static Context appContext;
    private final SharedPreferences sharedPreferences;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        appContext = context;
        try {
            DAY_SCHEDULE_NOTIFICATION = nameof(User.class, "dayScheduleNotification");
            BIG_EVENTS_NOTIFICATION = nameof(User.class, "bigEventsNotification");
            CHAT_PRIVATE_NOTIFICATION = nameof(User.class, "chatPrivateNotification");
            CHAT_GROUP_NOTIFICATION = nameof(User.class, "chatGroupNotification");
            SHARE_LOCALIZATION = nameof(User.class, "shareLocalization");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        String messagingToken = sharedPreferences.getString(Const.MESSAGING_TOKEN, "");
        int appVersion = getAppVersion();

        if (userListenerRegistration != null)
            userListenerRegistration.remove();

        sharedPreferences.edit().clear().apply();
        sharedPreferences.edit().putBoolean(Const.FIRST_START, false).apply();
        StreamSupport.stream(Const.TAB_FRAGMENTS).forEach(tabFragment -> sharedPreferences.edit()
                .putBoolean(tabFragment + Const.FIRST_START_POPUP_SUFFIX, false).apply());
        sharedPreferences.edit().putString(Const.MESSAGING_TOKEN, messagingToken).apply();
        sharedPreferences.edit().putInt(APP_VERSION, appVersion).apply();
    }

    public boolean getValue(String name) {
        return sharedPreferences.getBoolean(name, true);
    }

    public boolean getValueWithDefaultFalse(String name) {
        return sharedPreferences.getBoolean(name, false);
    }

    public float getValueFloat(String name) {
        return sharedPreferences.getFloat(name, (float) 0.0);
    }

    public String getValueString(String name) {
        return sharedPreferences.getString(name, "");
    }

    public void setValue(String name, boolean value) {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putBoolean(name, value);
        e.apply();
    }

    public void setValue(String name, String value) {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString(name, value);
        e.apply();
    }

    public void setValueFloat(String name, float value) {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putFloat(name, value);
        e.apply();
    }

    public void setUserListenerRegistration(ListenerRegistration registration) {
        userListenerRegistration = registration;
    }

    public int getAppVersion(){
        return sharedPreferences.getInt(APP_VERSION, -1);
    }

    public String getCountry() {
        return sharedPreferences.getString(USER_COUNTRY, "");
    }

    public void saveUserInfo(User user) {
        SharedPreferences.Editor e = sharedPreferences.edit();

        e.putString(SHARE_KEY_NAME, user.getName());
        e.putString(SHARE_KEY_EMAIL, user.getEmail());
        e.putString(SHARE_KEY_AVATAR, user.getAvatar());
        e.putString(SHARE_KEY_USER_ID, user.getId());
        e.putString(USER_CITY, user.getCity());
        e.putString(USER_COUNTRY, user.getCountry());
        e.putBoolean(DAY_SCHEDULE_NOTIFICATION, user.isDayScheduleNotification());
        e.putBoolean(BIG_EVENTS_NOTIFICATION, user.isBigEventsNotification());
        e.putBoolean(CHAT_GROUP_NOTIFICATION, user.isChatGroupNotification());
        e.putBoolean(CHAT_PRIVATE_NOTIFICATION, user.isChatPrivateNotification());
        e.putBoolean(SHARE_LOCALIZATION, user.isShareLocalization());
        e.putBoolean(IS_ONLINE, user.isOnline());

        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            e.putInt(APP_VERSION, versionCode);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        e.apply();
    }

    public User getUserInfo() {

        User user = new User();

        // SETTINGS
        user.setDayScheduleNotification(getValue(DAY_SCHEDULE_NOTIFICATION));
        user.setBigEventsNotification(getValue(BIG_EVENTS_NOTIFICATION));
        user.setChatPrivateNotification(getValue(CHAT_PRIVATE_NOTIFICATION));
        user.setChatGroupNotification(getValue(CHAT_GROUP_NOTIFICATION));
        user.setShareLocalization(getValue(SHARE_LOCALIZATION));

        user.setCity(sharedPreferences.getString(USER_CITY, ""));
        user.setCountry(sharedPreferences.getString(USER_COUNTRY, ""));
        user.setName(sharedPreferences.getString(SHARE_KEY_NAME, ""));
        user.setEmail(sharedPreferences.getString(SHARE_KEY_EMAIL, ""));
        user.setAvatar(sharedPreferences.getString(SHARE_KEY_AVATAR, "DEFAULT"));
        user.setId(sharedPreferences.getString(SHARE_KEY_USER_ID, ""));
        user.setToken(sharedPreferences.getString(Const.MESSAGING_TOKEN, ""));
        user.setOnline(getValue(IS_ONLINE));

        return user;
    }

    public void removeAll() {
        sharedPreferences.edit().clear().apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(SHARE_KEY_USER_ID, "");
    }


}
