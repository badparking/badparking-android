package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

import java.text.DecimalFormat;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * @author Dima Kovalenko & Vladimir Dranik
 */
public class LocationFragment extends BaseFragment{

    private static final String TAG = LocationFragment.class.getName();

    @InjectView(R.id.positioning_text_view)
    private TextView positioningText;
    @InjectView(R.id.next_button)
    private Button nextButton;

    public static Fragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(GeolocationState.INST);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).moveToNext();
            }
        });
        nextButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        GeolocationState.INST.getLocationManager().requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GeolocationState.WAITING_TIME_MILLIS,
                GeolocationState.ACCURANCY_IN_METERS,
                locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        GeolocationState.INST.getLocationManager().removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null) {

                Address address = GeolocationState.INST.getAddress(location);

                if (address != null) {
                    ClaimState.INST.getClaim().setCity(address.getLocality());
                    ClaimState.INST.getClaim().setAddress(address.getAddressLine(0));
                    positioningText.setText(ClaimState.INST.getFullAddress());
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    positioningText.setText("Miсцезнаходження визначається …");
                    nextButton.setVisibility(View.GONE);
                }

                DecimalFormat df = new DecimalFormat("#.######");
                ClaimState.INST.getClaim().setLatitude(df.format(location.getLatitude()).replace(",", "."));
                ClaimState.INST.getClaim().setLongitude(df.format(location.getLongitude()).replace(",", "."));
                GeolocationState.INST.mapPositioning(location.getLatitude(), location.getLongitude());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
}

