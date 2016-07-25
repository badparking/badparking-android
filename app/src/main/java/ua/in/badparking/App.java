package ua.in.badparking;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.services.UserState;

/**
 * Created by Dima Kovalenko on 5/5/16.
 */
public class App extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
//        }
        GeolocationState.INST.init(this);
        UserState.INST.init(this);
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }
}
