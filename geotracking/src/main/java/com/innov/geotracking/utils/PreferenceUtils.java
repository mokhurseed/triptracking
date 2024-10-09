package com.innov.geotracking.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides convenience methods and abstraction for storing data in the
 * {@link SharedPreferences}
 */
public class PreferenceUtils {

    public static final String APP_PREF = "GeoTracking";

    private SharedPreferences mSettings;
    private Editor mEditor;

    public PreferenceUtils(Context mContext) {
        mSettings = mContext.getSharedPreferences(APP_PREF, MODE_PRIVATE);
        mEditor = mSettings.edit();
    }

    /***
     * Set a value for the key
     ****/
    public void setValue(String key, String value) {
        mEditor.putString(key, value);
        mEditor.apply();
    }

    /***
     * Set a value for the key
     ****/
    public void setValue(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.apply();
    }

    /***
     * Set a value for the key
     ****/
    public void setValue(String key, double value) {
        setValue(key, Double.toString(value));
    }

    /***
     * Set a value for the key
     ****/
    public void setValue(String key, long value) {
        mEditor.putLong(key, value);
        mEditor.apply();
    }

    /****
     * Gets the value from the settings stored natively on the device.
     **/
    public String getValue(String key) {
        return mSettings.getString(key, "");
    }

    public int getIntValue(String key, int defaultValue) {
        return mSettings.getInt(key, defaultValue);
    }

    public long getLongValue(String key, long defaultValue) {
        return mSettings.getLong(key, defaultValue);
    }

    /****
     * Gets the value from the preferences stored natively on the device.
     *
     * @param defValue Default value for the key, if one is not found.
     **/
    public boolean getValue(String key, boolean defValue) {
        return mSettings.getBoolean(key, defValue);
    }

    public void setValue(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.apply();
    }

    public Boolean putStringSet(HashSet<String> cookies) {
        return mEditor.putStringSet("cookies",cookies).commit();
    }

    public Set<String> getStringSet(){
        return mSettings.getStringSet("cookies",new HashSet<String>());
    }
    /****
     * Clear all the preferences store in this {@link Editor}
     ****/
    public void clear() {
        mEditor.clear().commit();
    }

    /**
     * Removes preference entry for the given key.
     */
    public void removeValue(String key) {
        if (mEditor != null) {
            mEditor.remove(key).commit();
        }
    }
}
