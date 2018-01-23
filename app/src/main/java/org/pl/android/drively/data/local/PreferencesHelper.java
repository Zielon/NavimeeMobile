package org.pl.android.drively.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.pl.android.drively.data.model.User;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.util.Const;

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "android_boilerplate_pref_file";

    private final SharedPreferences mPref;
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_AVATA = "avatar";
    private static String SHARE_KEY_USER_ID = "id";
    private static String SHARE_KEY_UID = "uid";
    
    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public boolean getValue(String name) {
       return  mPref.getBoolean(name, true);
    }

    public boolean getValueWithDefaultFalse(String name) {
        return  mPref.getBoolean(name, false);
    }

    public float getValueFloat(String name) {
        return  mPref.getFloat(name, (float) 0.0);
    }

    public String getValueString(String name) {
        return  mPref.getString(name,"");
    }


    public void setValue(String name, boolean value) {
        //  Make a new preferences editor
        SharedPreferences.Editor e = mPref.edit();

        //  Edit preference to make it false because we don't want this to run again
        e.putBoolean(name, value);
        //  Apply changes
        e.apply();
    }



    public void setValue(String name, String value) {
        //  Make a new preferences editor
        SharedPreferences.Editor e = mPref.edit();

        //  Edit preference to make it false because we don't want this to run again
        e.putString(name, value);

        //  Apply changes
        e.apply();
    }

    public void setValueFloat(String name, float value) {
        //  Make a new preferences editor
        SharedPreferences.Editor e = mPref.edit();

        //  Edit preference to make it false because we don't want this to run again
        e.putFloat(name, value);

        //  Apply changes
        e.apply();
    }

    public void saveUserInfo(User user) {
        SharedPreferences.Editor e = mPref.edit();
        e.putString(SHARE_KEY_NAME, user.getName());
        e.putString(SHARE_KEY_EMAIL, user.getEmail());
        e.putString(SHARE_KEY_AVATA, user.getAvatar());
        e.putString(SHARE_KEY_USER_ID, user.getId());
        e.putString(SHARE_KEY_UID, Const.UID);
        e.apply();
    }

    public User getUserInfo(){

        String userName = mPref.getString(SHARE_KEY_NAME, "");
        String email = mPref.getString(SHARE_KEY_EMAIL, "");
        String avatar = mPref.getString(SHARE_KEY_AVATA, "DEFAULT");
        String id = mPref.getString(SHARE_KEY_USER_ID, "");

        User user = new User();

        user.setName(userName);
        user.setEmail(email);
        user.setAvatar(avatar);
        user.setId(id);

        return user;
    }

    public String getUID(){
        return mPref.getString(SHARE_KEY_UID, "");
    }
}
