package ua.in.badparking.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.android.AndroidProgressCallback;
import org.jdeferred.impl.DeferredObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Igor on 14-08-2015.
 */
public class Geolocation {

    private static final String TAG = "Geolocation";

    private final LocationManager locationManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AndroidDoneCallback<Location> doneCallback = new AndroidDoneCallback<Location>() {
        @Override
        public AndroidExecutionScope getExecutionScope() {
            return AndroidExecutionScope.BACKGROUND;
        }

        @Override
        public void onDone(Location result) {
            Log.i(TAG, "Location result - " + result);
            updateLocation(result);
        }
    };
    private final AndroidFailCallback<Throwable> failCallback = new AndroidFailCallback<Throwable>() {
        @Override
        public AndroidExecutionScope getExecutionScope() {
            return AndroidExecutionScope.BACKGROUND;
        }

        @Override
        public void onFail(Throwable result) {
            Log.e(TAG, "Failed to get location ", result);
        }
    };

    public boolean gpsEnabled;
    public boolean netEnabled;
    private Location actualLocation;
    private long locationUpdateTimestamp;

    private UpdatedLocationCallback updatedLocationCallback;

    public Geolocation(final Context context, final boolean gpsEnabled, final boolean netEnabled, final UpdatedLocationCallback updatedLocationCallback) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.gpsEnabled = gpsEnabled;
        this.netEnabled = netEnabled;
        this.updatedLocationCallback = updatedLocationCallback;
    }

    private DeferredObject<Location, Throwable, String> getLocation(@NonNull final String locationProvider,
                                                                   @NonNull final AndroidDoneCallback<Location> doneCallback,
                                                                   @NonNull final AndroidFailCallback<Throwable> failCallback,
                                                                   @Nullable final AndroidProgressCallback<String> progressCallback) {
        final DeferredObject<Location, Throwable, String> deferred = new DeferredObject<>();
        deferred.promise().done(doneCallback).fail(failCallback);

        if (progressCallback != null) deferred.promise().progress(progressCallback);

        final Location location = locationManager.getLastKnownLocation(locationProvider);

        Log.i(TAG, "Location - " + location);
        if (location == null) deferred.reject(new Exception("Could not get location for provider - " + locationProvider));
        else deferred.resolve(location);

        return deferred;
    }

    public void getLocation() {

        if (gpsEnabled) {
            getLocation(LocationManager.GPS_PROVIDER, doneCallback, failCallback, null);
        }

        if (netEnabled) {
            getLocation(LocationManager.GPS_PROVIDER, doneCallback, failCallback, null);
        }

        getLocation(LocationManager.PASSIVE_PROVIDER, doneCallback, failCallback, null);
    }

    public void setGpsEnabled(boolean enabled) {
        gpsEnabled = enabled;
    }

    public void setNetEnabled(boolean enabled) {
        netEnabled = enabled;
    }

    private void updateLocation(final Location location) {
        if (actualLocation == null || actualLocation.getAccuracy() > location.getAccuracy()) {
            actualLocation = location;
            updatedLocationCallback.locationUpdate(location);
        }
    }

    public void updateLocation() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                getLocation();
            }
        });
    }

    public interface UpdatedLocationCallback {

        void locationUpdate(final Location location);
    }
}
