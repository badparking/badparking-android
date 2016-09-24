package ua.in.badparking.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import ua.in.badparking.model.User;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum UserState {
    INST;

    private static final String USER_DATA_PREFS = "userDataPrefs";
    private static final String USER_TOKEN_KEY = "userTokenKey";
    private static final String USER_KEY = "userKey";

    private SharedPreferences userDataPrefs;
    Gson gson = new Gson();

    private User user = null;

    public void init(Context appContext) {
        userDataPrefs = appContext.getSharedPreferences(USER_DATA_PREFS, Context.MODE_PRIVATE);
    }

    public String getUserToken() {
        return userDataPrefs.getString(USER_TOKEN_KEY, null);
    }

    @SuppressWarnings("All")
    public void setUserToken(String token) {
        userDataPrefs.edit().putString(USER_TOKEN_KEY, token).commit();
    }

    public User getUser() {
        String userJson = userDataPrefs.getString(USER_KEY, null);
        if (userJson != null) {
            Type fooType = new TypeToken<User>() {
            }.getType();

            user = gson.fromJson(userJson, fooType);
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;

        String userJson = gson.toJson(user);
        userDataPrefs.edit().putString(USER_KEY, userJson).commit();
    }
}
