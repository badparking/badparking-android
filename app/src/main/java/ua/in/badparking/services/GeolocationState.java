package ua.in.badparking.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public enum GeolocationState {
    INST;

    private static final String TAG = GeolocationState.class.getName();
    public static final long WAITING_TIME_MILLIS = 3000L;
    public static final float ACCURANCY_IN_METERS = 3f;

    private Context context;
    private LocationManager locationManager;
    private Location location;
    private Geocoder geocoder;
    private Marker userMarker;

    public void init(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        geocoder = new Geocoder(context, Locale.getDefault());
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

    public Marker getUserMarker() {
        return userMarker;
    }

    public void setUserMarker(Marker userMarker) {
        this.userMarker = userMarker;
    }

    public void mapPositioning(GoogleMap mMap, double latitude, double longitude) {
        if (userMarker != null) {
            userMarker.remove();
        }

        LatLng coordinates = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 17));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(17)
                    .bearing(45)
                     //.tilt(45)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude)));
        }
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public Location getLocation() {
        return location;
    }
}