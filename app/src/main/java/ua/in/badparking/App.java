package ua.in.badparking;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.services.UserState;

/**
 * Created by Dima Kovalenko on 5/5/16.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
//        }
        //GeolocationState.INST.init(this);
        UserState.INST.init(this);
    }
}
