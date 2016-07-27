package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * @author Dima Kovalenko & Vladimir Dranik
 */
public class LocationFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener{

    private static final String TAG = LocationFragment.class.getName();

    private GoogleMap mMap;
    @BindView(R.id.positioning_text_view)
    TextView positioningText;
    @BindView(R.id.next_button)
    Button nextButton;
    private Unbinder unbinder;

    public static Fragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

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
            if(location != null && mMap != null) {
                Address address = GeolocationState.INST.getAddress(location);

                if (address != null) {
                    ClaimState.INST.getClaim().setCity(address.getLocality());
                    ClaimState.INST.getClaim().setAddress(address.getAddressLine(0));
                    positioningText.setText(ClaimState.INST.getFullAddress());
                    nextButton.setVisibility(View.VISIBLE);
                }

                DecimalFormat df = new DecimalFormat("#.######");
                ClaimState.INST.getClaim().setLatitude(df.format(location.getLatitude()).replace(",", "."));
                ClaimState.INST.getClaim().setLongitude(df.format(location.getLongitude()).replace(",", "."));
                GeolocationState.INST.mapPositioning(mMap, location.getLatitude(), location.getLongitude());
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
        if(mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()){
            Toast toast = Toast.makeText(getContext(), "Будь ласка, зачекайте для більш точного геопозиціювання", Toast.LENGTH_LONG);
            LinearLayout layout = (LinearLayout) toast.getView();
            if (layout.getChildCount() > 0) {
                TextView tv = (TextView) layout.getChildAt(0);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

