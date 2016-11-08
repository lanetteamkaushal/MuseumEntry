package com.guam.museumentry.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import static android.content.Context.MODE_PRIVATE;

public class Storage {

    private static SharedPreferences mSharedPreferences = null;
    String value;

    public static void putString(Context context, String key, String value) {

        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        Editor editor = mSharedPreferences.edit();
        try {
            editor.putString(key, value).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(Context context, String key) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        try {
            return mSharedPreferences.getString(key, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void putBoolean(Context context, String key, Boolean value) {

        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        Editor editor = mSharedPreferences.edit();
        try {
            editor.putBoolean(key, value).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Boolean getBoolean(Context context, String key) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        try {
            return mSharedPreferences.getBoolean(key, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void putIneger(Context context, String key, int value) {

        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        Editor editor = mSharedPreferences.edit();
        try {
            editor.putInt(key, value).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getInteger(Context context, String key) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(Constants.MuseumPreference, MODE_PRIVATE);
        }
        try {
            return mSharedPreferences.getInt(key, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

}
