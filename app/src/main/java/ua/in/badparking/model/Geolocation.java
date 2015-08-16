package ua.in.badparking.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.android.AndroidProgressCallback;
import org.jdeferred.impl.DeferredObject;

/**
 * Created by Igor on 14-08-2015.
 */
public class Geolocation {

    private static final String TAG = "Geolocation";

    private final LocationManager locationManager;

    public Geolocation(final Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void getLocation(@NonNull final String locationProvider,
                            @NonNull final AndroidDoneCallback<Location> doneCallback,
                            @NonNull final AndroidFailCallback<Throwable> failCallback,
                            @Nullable final AndroidProgressCallback<String> progressCallback) {

        final DeferredObject<Location, Throwable, String> deferred = new DeferredObject<>();
        deferred.promise().done(doneCallback).fail(failCallback);

        if (progressCallback != null) deferred.promise().progress(progressCallback);

        final Location location = locationManager.getLastKnownLocation(locationProvider);

        Log.i(TAG, "Location - " + location);

        if (location.hasAccuracy()) {
            float accuracy = location.getAccuracy();
            Log.i(TAG, "Accuracy - " + accuracy);

            Log.i(TAG, "Location provider " + locationProvider + " is enabled - " + locationManager.isProviderEnabled(locationProvider));


            if (accuracy > 5) {
                Log.i(TAG, "Accuracy more then 5 - " + accuracy);

                deferred.resolve(location);

            } else {
                Log.i(TAG, "Accuracy less then 5 - " + accuracy);
                deferred.resolve(location);
            }
        } else {
            Log.i(TAG, "No accuracy");
        }
    }

}
