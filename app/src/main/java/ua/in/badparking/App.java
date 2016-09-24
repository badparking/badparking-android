package ua.in.badparking;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import ua.in.badparking.services.GeolocationState;

/**
 * Created by Dima Kovalenko on 5/5/16.
 */
public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        if (!BuildConfig.DEBUG) {
        Crashlytics.start(this);
//        }
        GeolocationState.INST.init(this);
    }

}