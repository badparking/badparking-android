package ua.in.badparking.services;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeocoderAsynkTask extends AsyncTask<LatLng, Void, Address> {

    private Geocoder geocoder;

    public GeocoderAsynkTask(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    @Override
    protected Address doInBackground(LatLng... params) {
            LatLng latLng = params[0];
            Address address = null;

            try {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addressList != null && !addressList.isEmpty()) {
                    address = addressList.get(0);
                }
            } catch (IOException e) {
                return null;
            }

        return address;
    }
}
