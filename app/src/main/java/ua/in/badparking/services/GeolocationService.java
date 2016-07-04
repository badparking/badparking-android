package ua.in.badparking.services;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Igor on 14-08-2015.
 */
public enum GeolocationService {
    INST;

    private static final String TAG = "Geolocation";

    private LocationManager locationManager;

    public void init(final Context context) {
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void subscribe(ILocationListener listener) {
        // TODO for Igor
    }

    public boolean isLocationObsolete() {
        return true; // TODO for Igor
    }

    public interface ILocationListener {
        void onLocationObtained(Location location);
    }
}
