package ua.in.badparking.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import ua.in.badparking.ui.dialogs.Alerts;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by User on 11/21/2016.
 */

public class PermissionsChecker implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private Activity activity;
    private Alerts alerts;
    private GoogleApiClient googleApiClient;
    final private int REQUEST_CHECK_GPS = 125;

    public PermissionsChecker(Activity activity) {
        this.activity = activity;
        alerts = new Alerts(activity);
        googleApiClient = initGoogleApiClient();
    }

    private GoogleApiClient initGoogleApiClient(){
        return new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    public boolean confirmGPSEnabled() {
        LocationManager lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        final boolean isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isAvailable) {
            //alerts.showGpsAlert();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(activity, REQUEST_CHECK_GPS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }

        return isAvailable;
    }

    public boolean confirmAirplaneModeOff() {
        boolean isOff = Settings.System.getInt(activity.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 0;

        if (!isOff) {
            alerts.showAirModeAlert();
        }

        return isOff;
    }

    public boolean confirmWiFiAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = (wifi != null && wifi.isConnectedOrConnecting()) ||
                (mobile != null && mobile.isConnectedOrConnecting());

        if (!isConnected) {
            alerts.showWifiAlert();
        }

        return isConnected;
    }

    public boolean confirmPermissionsAvailable() {
        return confirmAirplaneModeOff() && confirmWiFiAvailable() && confirmGPSEnabled();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}
