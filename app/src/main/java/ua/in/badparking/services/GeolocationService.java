package ua.in.badparking.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import ua.in.badparking.events.LocationEvent;

public enum GeolocationService {
    INST;

    private static final String TAG = GeolocationService.class.getName();
    public static final long WAITING_TIME_MILLIS = 3000L;
    public static final float ACCURANCY_IN_METERS = 3f;

    private Context context;
    private LocationManager locationManager;
    private Location lastLocation;
    private Geocoder geocoder;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            DecimalFormat df = new DecimalFormat("#.######");
            ClaimService.INST.getClaim().setLatitude(df.format(location.getLatitude()).replace(",", "."));
            ClaimService.INST.getClaim().setLongitude(df.format(location.getLongitude()).replace(",", "."));
            EventBus.getDefault().post(new LocationEvent(location));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void start(Context context) {
        this.context = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        subscribeToLocationUpdates();
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    public void stop() {
        unsubscribeFromLocationUpdates();
    }

    public Address getAddress(double latitude, double longitude) {
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

            if (addressList != null && !addressList.isEmpty()) {
                return addressList.get(0);
            }

        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(context, "Помилка геопозиціонування", Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    public Location getLocation() {
        return lastLocation;
    }

    public void unsubscribeFromLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    public void subscribeToLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GeolocationService.WAITING_TIME_MILLIS,
                GeolocationService.ACCURANCY_IN_METERS,
                locationListener);

        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            locationListener.onLocationChanged(lastLocation);
        }
    }
}