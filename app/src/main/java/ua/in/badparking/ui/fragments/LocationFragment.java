package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.tajchert.sample.DotsTextView;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * @author Dima Kovalenko & Vladimir Dranik
 */
public class LocationFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = LocationFragment.class.getName();

    @BindView(R.id.dots)
    DotsTextView dotsTextView;

    @BindView(R.id.positioning_text_view)
    TextView positioningText;

    @BindView(R.id.next_button)
    Button nextButton;

    private GoogleMap mMap;

    private static boolean showHint = false;

    public static Fragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                SupportMapFragment fragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.mapFragment);
                fragment.getMapAsync(LocationFragment.this);
            }
        }, 30);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).moveToNext();
            }
        });
        nextButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            GeolocationState.INST.getLocationManager().requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    GeolocationState.WAITING_TIME_MILLIS,
                    GeolocationState.ACCURANCY_IN_METERS,
                    locationListener);
        } catch (SecurityException se) {
            Log.i(TAG, se.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            GeolocationState.INST.getLocationManager().removeUpdates(locationListener);
        } catch (SecurityException se) {
            Log.i(TAG, se.getMessage());
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (GeolocationState.INST.getUserMarker() == null && location != null && mMap != null) {
                Address address = GeolocationState.INST.getAddress(location.getLatitude(), location.getLongitude());
                setAddress(address);
                GeolocationState.INST.getUserMarker().remove();
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();

                Address address = GeolocationState.INST.getAddress(point.latitude, point.longitude);
                setAddress(address);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });

        mMap.setOnMarkerClickListener(this);

        if (mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException se) {
                Log.i(TAG, se.getMessage());
            }

            mMap.setOnMyLocationButtonClickListener(this);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void showTimePositioningHint() {
        if (!showHint) {
            Toast toast = Toast.makeText(getContext(), getResources().getText(R.string.please_wait_gps), Toast.LENGTH_LONG);
            LinearLayout layout = (LinearLayout)toast.getView();
            if (layout.getChildCount() > 0) {
                TextView tv = (TextView)layout.getChildAt(0);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            showHint = true;
        }
    }

    private void setAddress(Address address) {
        if (address == null) {
            ClaimState.INST.getClaim().setCity(null);
            ClaimState.INST.getClaim().setAddress(null);

            dotsTextView.showAndPlay();
            positioningText.setText(getResources().getText(R.string.positioning_in_progress));
            nextButton.setVisibility(View.GONE);

        } else {
            ClaimState.INST.getClaim().setCity(address.getLocality());
            ClaimState.INST.getClaim().setAddress(address.getAddressLine(0));

            dotsTextView.hideAndStop();
            positioningText.setText(ClaimState.INST.getFullAddress());
            nextButton.setVisibility(View.VISIBLE);

            DecimalFormat df = new DecimalFormat("#.######");
            ClaimState.INST.getClaim().setLatitude(df.format(address.getLatitude()).replace(",", "."));
            ClaimState.INST.getClaim().setLongitude(df.format(address.getLongitude()).replace(",", "."));
            GeolocationState.INST.mapPositioning(mMap, address.getLatitude(), address.getLongitude());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        GeolocationState.INST.getUserMarker().remove();
        GeolocationState.INST.setUserMarker(null);
        setAddress(null);
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            showTimePositioningHint();
        }
    }
}