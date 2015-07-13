package com.yeahdev.yeahurls.util;


import android.content.SharedPreferences;

import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;

public class SharedPreferencesHelper {

    public static enum RemoveType {
        UserCred,
        User,
        All
    }

    public static void setUserToPreferences(SharedPreferences preferences, User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("emailAddress", user.getEmailAddress());
        editor.putString("provider", user.getProvider());
        editor.apply();
    }

    public static void setUserCredsToPreferences(SharedPreferences preferences, UserCreds userCreds) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", userCreds.getUserId());
        editor.putLong("expireDate", userCreds.getExpireDate());
        editor.apply();
    }

    public static User getUserFromPreferences(SharedPreferences preferences) {
        User u = new User();
        u.setEmailAddress(preferences.getString("emailAddress", null));
        u.setProvider(preferences.getString("provider", null));
        return u;
    }

    public static UserCreds getUserCredsFromPreferences(SharedPreferences preferences) {
        UserCreds uc = new UserCreds();
        uc.setUserId(preferences.getString("userId", null));
        uc.setExpireDate(preferences.getLong("expireDate", 0));
        return uc;
    }

    public static void removeUserDataFromPreferences(SharedPreferences preferences, RemoveType type) {
        SharedPreferences.Editor editor = preferences.edit();

        switch (type) {
            case UserCred:
                editor.remove("userId");
                editor.remove("expireDate");
                editor.apply();
                break;

            case User:
                editor.remove("emailAddress");
                editor.remove("provider");
                editor.apply();
                break;

            case All:
                editor.remove("userId");
                editor.remove("expireDate");
                editor.remove("emailAddress");
                editor.remove("provider");
                editor.apply();
                break;
        }
    }
}
