package org.pl.android.navimee.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.pl.android.navimee.injection.ApplicationContext;

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "android_boilerplate_pref_file";

    private final SharedPreferences mPref;

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



}
