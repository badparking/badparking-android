package ua.in.badparking.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum UserService {
    INST;

    private static final String USER_DATA_PREFS = "userDataPrefs";
    private static final String USER_TOKEN_KEY = "userTokenKey";

    private SharedPreferences userDataPrefs;

    public void init(Context appContext) {
        userDataPrefs = appContext.getSharedPreferences(USER_DATA_PREFS, Context.MODE_PRIVATE);
    }

    public String getUserToken() {  // TODO SecurePrefs
        return userDataPrefs.getString(USER_TOKEN_KEY, null);
    }

    @SuppressWarnings("All")
    public void setUserToken(String token) {
        userDataPrefs.edit().putString(USER_TOKEN_KEY, token).commit();
    }

    public void onBankIdIntentObtained(Intent intent) {
        // TODO
    }
}
