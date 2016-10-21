package ua.in.badparking.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;

import ua.in.badparking.ui.dialogs.Alerts;
import ua.in.badparking.utils.Constants;

import static android.content.Context.LOCATION_SERVICE;


public class GpsStatusReceiver extends BroadcastReceiver {

    private Activity activity;

    @Override
    public void onReceive(Context context, Intent intent) {
        Alerts alerts = new Alerts(activity);

        if (intent.getAction().matches(Constants.PROVIDERS_CHANGED_ACTION)) {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            boolean isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isAvailable) {
                alerts.showGpsAlert();
            }
        }
    }

    public void start(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void stop(Context context) {
        context.unregisterReceiver(this);
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
