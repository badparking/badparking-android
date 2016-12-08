package ua.in.badparking;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.UserService;

/**
 * Created by Dima Kovalenko on 5/5/16.
 */
public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
//        if (!BuildConfig.DEBUG) {
        Crashlytics.start(this);
//        }
        UserService.INST.init(this);
        ClaimService.INST.init(this);
    }
}