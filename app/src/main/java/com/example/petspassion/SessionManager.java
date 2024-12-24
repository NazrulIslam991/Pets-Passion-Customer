package com.example.petspassion;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_UID = "user_uid";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user UID
    public void saveUserUID(String uid) {
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }

    // Get user UID
    public String getUserUID() {
        return sharedPreferences.getString(KEY_USER_UID, null);
    }

    // Check if the user is logged in
    public boolean isLoggedIn() {
        return getUserUID() != null;
    }

    // Clear user session
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
