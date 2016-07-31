package ua.in.badparking.events;

import android.location.Location;

/**
 * Created by Dima Kovalenko on 7/31/16.
 */
public class LocationEvent {
    private Location _location;

    public LocationEvent(Location location) {
        _location = location;
    }

    public Location getLocation() {
        return _location;
    }
}
