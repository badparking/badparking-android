package ua.in.badparking.receivers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import ua.in.badparking.utils.Constants;
import ua.in.badparking.utils.LogHelper;

public class UserLocationListener implements LocationListener {

    private Context context;

    public UserLocationListener(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        String threadId = LogHelper.threadId();
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "Location Monitoring Service onLocationChanged - " + threadId);

        String logMsg = LogHelper.formatLocationInfo(location);
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "Location Monitoring - " + logMsg);

        Intent sendLocationInfoIntent = new Intent(Constants.SEND_LOCATION_INFO_ACTION);
        sendLocationInfoIntent.putExtra(Constants.LATITUDE, location.getLatitude());
        sendLocationInfoIntent.putExtra(Constants.LONGITUDE, location.getLongitude());
        context.sendBroadcast(sendLocationInfoIntent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}