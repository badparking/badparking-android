package ua.in.badparking.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Volodymyr Dranyk on 7/25/2016.
 */
public enum GeolocationState {
    INST;

    private static final String TAG = "Geolocation";

    public static final long WAITING_TIME_MILLIS = 3000L;
    public static final float ACCURANCY_IN_METERS = 3f;

    private Context context;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private Location mLocation;
    private long lastUpdateTimestamp;

    public void setLocation(Location location) {
        mLocation = location;
        lastUpdateTimestamp = System.currentTimeMillis();
    }

    public void init(Context context) {
        this.context = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    public Address getAddress(Location location) {
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addressList != null && !addressList.isEmpty()) {
                return addressList.get(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Помилка геопозиціонування", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void mapPositioning(GoogleMap mMap, double latitude, double longitude) {
        LatLng coordinates = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(coordinates)
                    .zoom(17)
                    .bearing(90)
                    .tilt(0)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public boolean isLocationActual() {
        return System.currentTimeMillis() - lastUpdateTimestamp < 1000 * 60 * 2;
    }

    public Location getLastLocation() {
        return mLocation;
    }
}
