package ua.in.badparking.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum TrespassController {
    INST;

    public static final String USER_PREFS = "userPrefs";
    private Trespass trespass = new Trespass();
    private SharedPreferences sharedPreferences;


    public Trespass getTrespass() {
        return trespass;
    }

    public boolean isSenderInfoFulfilled() {
        return !TextUtils.isEmpty(trespass.getEmail()) &&
                !TextUtils.isEmpty(trespass.getName()) &&
                !TextUtils.isEmpty(trespass.getPhone());

    }

    public void restoreFromPrefs(Context applicationContext) {
        sharedPreferences = applicationContext.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        trespass.setName(sharedPreferences.getString("propertyName", null));
        trespass.setPhone(sharedPreferences.getString("propertyPhone", null));
        trespass.setEmail(sharedPreferences.getString("propertyEmail", null));
        trespass.setCity(sharedPreferences.getString("propertyCity", null));
    }

    public void saveToPrefs() {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("propertyName", trespass.getName());
        editor.putString("propertyPhone", trespass.getPhone());
        editor.putString("propertyEmail", trespass.getEmail());
        editor.putString("propertyCity", trespass.getCity());
        editor.apply();
    }
}
