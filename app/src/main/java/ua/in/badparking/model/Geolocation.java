package ua.in.badparking.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Igor on 14-08-2015.
 */
public class Geolocation {

    private static final String TAG = "Geolocation";

    private final LocationManager locationManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Geocoder geocoder;

    private Location actualLocation;
    private long locationUpdateTimestamp;
    private Handler handler;

    public Geolocation(final Context context,
                       final Handler.Callback handlerCallback) {
        handler = new Handler(Looper.myLooper(), handlerCallback);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final Locale locale = new Locale("ru", "RU");
        geocoder = new Geocoder(context, locale);

//        LocationServices.FusedLocationApi.getLastLocation();


    }

    private void getLocation(final Deferred<Location, Throwable, Void> finalDeferred) {

        final DeferredManager deferredManager = new DefaultDeferredManager();
        final List<Deferred> deferredList = new LinkedList<>();

        {
            final Deferred<Location, Throwable, Void> deferred = new DeferredObject<>();
            deferredList.add(deferred);
            new LocationAsync(locationManager) {
                @Override
                public void onPostExecute(Location location) {
                    Log.i(TAG, "GPS post execute with location - " + location);
                    if (location == null)
                        deferred.reject(new Exception("Failed to get location from " + LocationManager.GPS_PROVIDER));
                    else deferred.resolve(location);
                }

            }.executeOnExecutor(executorService, LocationManager.GPS_PROVIDER);
        }

        {
            final Deferred<Location, Throwable, Void> deferred = new DeferredObject<>();
            deferredList.add(deferred);
            new LocationAsync(locationManager) {

                @Override
                public void onPostExecute(Location location) {
                    Log.i(TAG, "NET post execute with location - " + location);
                    if (location == null)
                        deferred.reject(new Exception("Failed to get location from " + LocationManager.NETWORK_PROVIDER));
                    else deferred.resolve(location);
                }

            }.executeOnExecutor(executorService, LocationManager.NETWORK_PROVIDER);
        }

        final Deferred<Location, Throwable, Void> deferred = new DeferredObject<>();
        deferredList.add(deferred);

        new LocationAsync(locationManager) {

            @Override
            public void onPostExecute(Location location) {
                Log.i(TAG, "Passive post execute with location - " + location);
                if (location == null)
                    deferred.reject(new Exception("Failed to get location from " + LocationManager.PASSIVE_PROVIDER));
                else deferred.resolve(location);
            }

        }.executeOnExecutor(executorService, LocationManager.PASSIVE_PROVIDER);

        Deferred[] deferreds = new Deferred[deferredList.size()];
        deferreds = deferredList.toArray(deferreds);

        deferredManager.when(deferreds).done(new AndroidDoneCallback<MultipleResults>() {
            @Override
            public AndroidExecutionScope getExecutionScope() {
                return AndroidExecutionScope.BACKGROUND;
            }

            @Override
            public void onDone(MultipleResults result) {
                Log.i(TAG, "On location done");
                final List<Location> locations = new ArrayList<>();
                Location maxAccuracyLocation = null;

                for (OneResult oneResult : result) {
                    final Location location = (Location)oneResult.getResult();
                    locations.add(location);
                    if (maxAccuracyLocation == null || maxAccuracyLocation.getAccuracy() > location.getAccuracy())
                        maxAccuracyLocation = location;
                }

                if (maxAccuracyLocation != null) finalDeferred.resolve(maxAccuracyLocation);
                else finalDeferred.reject(new Exception("Failed to get location"));
            }
        });

    }

    public void updateLocation() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Started updating location");
                final Deferred<Location, Throwable, Void> deferred = new DeferredObject<>();
                deferred.done(new AndroidDoneCallback<Location>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onDone(Location result) {
                        Log.i(TAG, "On done updating locations");
                        final Bundle data = new Bundle();
                        data.putDouble("Latitude", result.getLatitude());
                        data.putDouble("Longitude", result.getLongitude());
                        data.putDouble("Accuracy", result.getAccuracy());

                        final Message message = new Message();
                        message.what = 2;
                        message.setData(data);

                        handler.sendMessage(message);
                    }
                }).fail(new AndroidFailCallback<Throwable>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onFail(Throwable result) {
                        Log.e(TAG, "Failed to get location from any service");
                    }
                });

                getLocation(deferred);
            }
        });
    }

    public void requestCurrentAddressesOptions(final int maxAddresses) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Started retrieving address");
                final Deferred<Location, Throwable, Void> deferred = new DeferredObject<>();
                deferred.done(new AndroidDoneCallback<Location>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onDone(Location result) {
                        Log.i(TAG, "Retrieved location - " + result + ", now getting addresses");

                        new AsyncTask<Location, Void, List<Address>>() {

                            @Override
                            protected List<Address> doInBackground(Location... params) {
                                ArrayList<Address> addresses = null;
                                try {
                                    addresses = new ArrayList<>(geocoder.getFromLocation(params[0].getLatitude(), params[0].getLongitude(), maxAddresses));
                                    Log.i(TAG, "Retrieved addresses - " + addresses);
                                    final Bundle data = new Bundle();
                                    data.putParcelableArrayList("addresses", addresses);

                                    final Message msg = new Message();
                                    msg.what = 1;
                                    msg.setData(data);

                                    handler.sendMessage(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return addresses;
                            }
                        }.executeOnExecutor(executorService, result);
                    }
                }).fail(new AndroidFailCallback<Throwable>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onFail(Throwable result) {
                        Log.e(TAG, "Failed to get location from any service");
                    }
                });

                getLocation(deferred);
            }
        });
    }

    public Deferred<List<Address>, Throwable, Void> requestPositionAddressesOptions(final LatLng latLng, final int maxAddresses) {
        final Deferred<List<Address>, Throwable, Void> result = new DeferredObject<>();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<LatLng, Void, List<Address>>() {

                    @Override
                    protected List<Address> doInBackground(LatLng... params) {
                        ArrayList<Address> addresses = null;
                        try {
                            addresses = new ArrayList<>(geocoder.getFromLocation(params[0].latitude, params[0].longitude, maxAddresses));
                            Log.i(TAG, "Retrieved addresses - " + addresses);
                            final Bundle data = new Bundle();
                            data.putParcelableArrayList("addresses", addresses);

                            final Message msg = new Message();
                            msg.what = 1;
                            msg.setData(data);

                            handler.sendMessage(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return addresses;
                    }
                }.executeOnExecutor(executorService, latLng);
            }
        });

        return result;
    }

    private static class LocationAsync extends AsyncTask<String, Void, Location> {

        private final LocationManager locationManager;

        public LocationAsync(final LocationManager locationManager) {
            this.locationManager = locationManager;
        }

        @Override
        protected Location doInBackground(String... params) {
            return locationManager.getLastKnownLocation(params[0]);
        }
    }
}
