package ua.in.badparking.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Volodymyr Dranyk on 7/25/2016.
 */
public enum GeolocationState implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {
    INST;

    private static final String TAG = "Geolocation";

    public static final int WAITING_TIME_MILLIS = 0;
    public static final int ACCURANCY_IN_METERS = 3;

    private Context context;
    private GoogleMap mMap;
    //private Location location;
    private LocationManager locationManager;
    private Geocoder geocoder;

    public void init(final Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMap != null) {
            UiSettings uiSettings = mMap.getUiSettings();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setTiltGesturesEnabled(false);
            uiSettings.setCompassEnabled(false);
        }
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

    public void mapPositioning(double latitude, double longitude){
        LatLng coordinates = new LatLng(latitude, longitude);
        if(mMap!=null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(coordinates)
                    .zoom(17)
                    .bearing(90)
                    .tilt(40)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}
