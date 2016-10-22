package ua.in.badparking.ui.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.GeocoderAsynkTask;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.utils.Constants;
import ua.in.badparking.utils.LogHelper;

import static android.content.Context.LOCATION_SERVICE;

/**
 * @author Dima Kovalenko & Vladimir Dranik
 */
public class LocationFragment extends BaseFragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = LocationFragment.class.getName();
    private static final int LOCATION_ZOOM = 17;

    @BindView(R.id.positioning_text_view)
    TextView positioningText;

    @BindView(R.id.next_button)
    Button nextButton;

    private GoogleMap mMap;
    private Marker marker;

    private LocationInfoReceiver locationInfoReceiver;
    private IntentFilter intentFilter;
    private Geocoder geocoder;
    private SupportMapFragment mMapFragment;

    private LatLng currentLocation;
    private Address currentAddress;
    private boolean isFirstAnimate = true;

    public static BaseFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocationManager lm = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE); //вынести в общие

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // permission logic
        }

        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        if (location != null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            currentAddress = getCurrentAddress(currentLocation);
        }

        locationInfoReceiver = new LocationInfoReceiver();
        intentFilter = new IntentFilter(Constants.SEND_LOCATION_INFO_ACTION);
        mMapFragment = SupportMapFragment.newInstance();

        getChildFragmentManager().beginTransaction().add(R.id.framelayout_location_container, mMapFragment).commit();
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mMapFragment.getMapAsync(LocationFragment.this);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClaimService.INST.getClaim().setLatitude(String.valueOf(currentLocation.latitude));
                ClaimService.INST.getClaim().setLongitude(String.valueOf(currentLocation.longitude));

                if (currentAddress == null) {
                    ClaimService.INST.getClaim().setCity("unrecognized");
                    ClaimService.INST.getClaim().setAddress("unrecognized");
                } else {
                    String city = currentAddress.getLocality();
                    String addressStr = currentAddress.getAddressLine(0);

                    ClaimService.INST.getClaim().setCity((city != null) ? city : "unrecognized");
                    ClaimService.INST.getClaim().setAddress((addressStr != null) ? addressStr : "unrecognized");
                }

                ((MainActivity)getActivity()).showPage(MainActivity.PAGE_CLAIM_OVERVIEW);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentLocation == null) {
            showTimePositioningHint();
            nextButton.setVisibility(View.GONE);
        } else {
            setAddressUI(currentAddress);
        }

        getActivity().registerReceiver(locationInfoReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(locationInfoReceiver);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // permission logic
            }

            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);


            mapPositioning(mMap, currentLocation);
        }
    }

    public void mapPositioning(GoogleMap mMap, LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }

        if (mMap != null) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(LOCATION_ZOOM)
                    .bearing(45)
                    //.tilt(45)
                    .build();

            // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            if (isFirstAnimate) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
                isFirstAnimate = false;
            } else {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1, null);
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void showTimePositioningHint() {
        Toast toast = Toast.makeText(getContext(), getResources().getText(R.string.please_wait_gps), Toast.LENGTH_LONG);
        LinearLayout layout = (LinearLayout)toast.getView();
        if (layout.getChildCount() > 0) {
            TextView tv = (TextView)layout.getChildAt(0);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setAddressUI(Address address) {
        if (address == null) {
            positioningText.setText(String.valueOf(currentLocation.longitude + ", " + currentLocation.longitude));
        } else {
            String city = (address.getLocality() != null) ? address.getLocality() + ", " : "";
            String addressStr = (address.getAddressLine(0) != null) ? address.getAddressLine(0) : "";

            if (city.isEmpty() && addressStr.isEmpty()) {
                String.valueOf(currentLocation.longitude + ", " + currentLocation.longitude);
            } else positioningText.setText(city + addressStr);
        }
    }

    private Address getCurrentAddress(LatLng latLng) {
        GeocoderAsynkTask geocoderAsynkTask = new GeocoderAsynkTask(geocoder);
        geocoderAsynkTask.execute(latLng);
        Address address;

        try {
            address = geocoderAsynkTask.get();
        } catch (InterruptedException | ExecutionException e) {
            address = null;
        }

        return address;
    }


    public class LocationInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(Constants.SEND_LOCATION_INFO_ACTION)) {
                String action = intent.getAction();
                Log.d(LogHelper.LOCATION_MONITORING_TAG, "Monitor Location BROADCAST **1** Intent Action:" + action);

                Bundle extras = intent.getExtras();
                double latitude = extras.getDouble(Constants.LATITUDE);
                double longitude = extras.getDouble(Constants.LONGITUDE);

                Log.d(LogHelper.LOCATION_MONITORING_TAG, "****Location Changed - " + latitude + " " + longitude);
                currentLocation = new LatLng(latitude, longitude);

                if (mMap != null) {
                    mapPositioning(mMap, currentLocation);
                }

                currentAddress = getCurrentAddress(currentLocation);
                setAddressUI(currentAddress);

                if (nextButton.getVisibility() == View.GONE) {
                    nextButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}