package ua.in.badparking.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import ua.in.badparking.data.Report;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ReportController {
    INST;

    public static final String USER_PREFS = "userPrefs";
    private Report report = new Report();
    private SharedPreferences sharedPreferences;


    public Report getReport() {
        return report;
    }

    public void restoreFromPrefs(Context applicationContext) {
        sharedPreferences = applicationContext.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        report.setCity(sharedPreferences.getString("propertyCity", null));
    }

    public void saveToPrefs() {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("propertyCity", report.getCity());
        editor.apply();
    }
}
