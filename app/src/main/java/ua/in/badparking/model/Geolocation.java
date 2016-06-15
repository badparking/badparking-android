package ua.in.badparking.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by Igor on 14-08-2015.
 */
public enum Geolocation {
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
