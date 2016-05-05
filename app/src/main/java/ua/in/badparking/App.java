package ua.in.badparking;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import ua.in.badparking.BuildConfig;
import ua.in.badparking.model.UserManager;

/**
 * Created by Dima Kovalenko on 5/5/16.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }
        UserManager.INST.init(this);
    }
}
