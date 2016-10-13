package ua.in.badparking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;

import ua.in.badparking.ui.dialogs.Alerts;

import static android.content.Context.LOCATION_SERVICE;


public class GpsStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Alerts alerts = new Alerts(context);
        String action = intent.getAction();

        if (action.equalsIgnoreCase("android.location.PROVIDERS_CHANGED")){
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            boolean isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(!isAvailable) {
                alerts.showGpsAlert();
            }
        }
    }

    public void start(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.PROVIDERS_CHANGED");
        context.registerReceiver(this, filter);
    }

    public void stop(Context context) {
        context.unregisterReceiver(this);
    }
}
