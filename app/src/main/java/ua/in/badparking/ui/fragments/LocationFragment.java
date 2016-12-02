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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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
        View.OnClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener,
        View.OnTouchListener {

    private static final String TAG = LocationFragment.class.getName();
    private static final int LOCATION_ZOOM = 17;

    @BindView(R.id.positioning_text_view)
    protected TextView positioningText;

    @BindView(R.id.next_button)
    protected Button nextButton;

    @BindView(R.id.myLocationButton)
    protected View myLocationButton;

    @BindView(R.id.mapOverlay)
    protected View mapOverlay;

    @BindView(R.id.customLocationIcon)
    protected ImageView customLocationIcon;

    private GoogleMap mMap;
    private Marker marker;

    private LocationInfoReceiver locationInfoReceiver;
    private IntentFilter intentFilter;
    private Geocoder geocoder;
    private SupportMapFragment mMapFragment;

    private Unbinder unbinder;

    private LatLng currentLocation;
    private LatLng customLocation;
    private Address address;
    private boolean isFirstAnimate = true;
    private boolean isOnCurrentPosition = true;
    private static final LatLng DISABLE_GPS_START_MAP_POINT = new LatLng(50.4501D, 30.523400000000038D);

    public static BaseFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        myLocationButton.setOnClickListener(this);
        Location location = null;

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        if (location != null && currentLocation == null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            address = getAddress(currentLocation);
            Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****FIRST NETWORK LAST_LOCATION " + currentLocation.latitude + " " + currentLocation.longitude);
        }

        locationInfoReceiver = new LocationInfoReceiver();
        intentFilter = new IntentFilter(Constants.SEND_LOCATION_INFO_ACTION);
        mMapFragment = SupportMapFragment.newInstance();

        mapOverlay.setOnTouchListener(this);
        getChildFragmentManager().beginTransaction().add(R.id.framelayout_location_container, mMapFragment).commit();
        mMapFragment.getMapAsync(LocationFragment.this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setEnabled(false);
                LatLng location = (isOnCurrentPosition) ? currentLocation : customLocation;
                ClaimService.INST.getClaim().setLatitude(String.valueOf(location.latitude));
                ClaimService.INST.getClaim().setLongitude(String.valueOf(location.longitude));

                if (address == null) {
                    ClaimService.INST.getClaim().setCity("unrecognized");
                    ClaimService.INST.getClaim().setAddress("unrecognized");
                } else {
                    String city = address.getLocality();
                    String addressStr = address.getAddressLine(0);

                    ClaimService.INST.getClaim().setCity((city != null) ? city : "unrecognized");
                    ClaimService.INST.getClaim().setAddress((addressStr != null) ? addressStr : "unrecognized");
                }

                ((MainActivity) getActivity()).showPage(MainActivity.PAGE_CLAIM_OVERVIEW);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentLocation == null) {
            showTimePositioningHint();
            currentLocation = DISABLE_GPS_START_MAP_POINT;
            //nextButton.setVisibility(View.GONE);
        } else {
            setAddressUI(address);
        }

        getActivity().registerReceiver(locationInfoReceiver, intentFilter);
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****Register LocationInfoReciver");
        claimStateLogging();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(locationInfoReceiver);
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****Unregister LocationInfoReciver!");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            mMap.setOnCameraIdleListener(this);
            mMap.getUiSettings().setCompassEnabled(false);

            LatLng location;
            if (isOnCurrentPosition) {
                location = currentLocation;
            } else location = customLocation;

            if(location != null) {
                mapPositioning(mMap, location);
                address = getAddress(location);
                setAddressUI(address);
            }

            if(currentLocation != null) {
                marker = getUpdatedCurrentLocationMarker();
                Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****onMapReady finished " + currentLocation.latitude + " " + currentLocation.longitude);
            }
        }
    }

    private Marker getUpdatedCurrentLocationMarker() {
        if (marker != null) {
            marker.remove();
        }

        Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****NEW MARKER POSITION - " + currentLocation.latitude + " " + currentLocation.longitude);

        return mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                //.title("Current location")
                .snippet("Current location")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mylocation_dot_24dp)));
    }

    private void mapPositioning(GoogleMap mMap, LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(LOCATION_ZOOM)
                .bearing(45)
                //.tilt(45)
                .build();

        if (isFirstAnimate) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
            isFirstAnimate = false;
        } else {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1, null);
        }
    }

    private void showTimePositioningHint() {
        Toast toast = Toast.makeText(getContext(), getResources().getText(R.string.please_check_gps), Toast.LENGTH_LONG);
        LinearLayout layout = (LinearLayout) toast.getView();
        if (layout.getChildCount() > 0) {
            TextView tv = (TextView) layout.getChildAt(0);
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

            if (city.isEmpty() & addressStr.isEmpty()) {
                String.valueOf(currentLocation.longitude + ", " + currentLocation.longitude);
            } else positioningText.setText(city + addressStr);
        }
    }

    private Address getAddress(LatLng latLng) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myLocationButton:
                customLocationIcon.setColorFilter(getContext().getResources().getColor(R.color.accent));

                if(currentLocation != null) {
                    mapPositioning(mMap, currentLocation);
                    isOnCurrentPosition = true;
                }

                break;
        }
    }

    @Override
    public void onCameraIdle() {
        if (!isOnCurrentPosition) {
            customLocationIcon.setColorFilter(getContext().getResources().getColor(R.color.red));
        }

        LatLng location = null;
        if (isOnCurrentPosition) {
            location = currentLocation;
        }

        if (!isOnCurrentPosition) {
            location = mMap.getCameraPosition().target;
            customLocation = location;
        }

        if(location != null) {
            address = getAddress(location);
            setAddressUI(address);
        }

        getActivity().registerReceiver(locationInfoReceiver, intentFilter);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        getActivity().unregisterReceiver(locationInfoReceiver);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isOnCurrentPosition = false;
                break;
        }

        return false;
    }

    public class LocationInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(Constants.SEND_LOCATION_INFO_ACTION)) {
                String action = intent.getAction();
                Log.d(LogHelper.LOCATION_MONITORING_TAG, "******Monitor Location BROADCAST **1** Intent Action:" + action);

                Bundle extras = intent.getExtras();
                double latitude = extras.getDouble(Constants.LATITUDE);
                double longitude = extras.getDouble(Constants.LONGITUDE);

                currentLocation = new LatLng(latitude, longitude);

                if (mMap != null) {
                    Log.d(LogHelper.LOCATION_MONITORING_TAG, "*****GPS LOCATION CHANGED - " + latitude + " " + longitude);

                    marker = getUpdatedCurrentLocationMarker();

                    if (isOnCurrentPosition) {
                        mapPositioning(mMap, currentLocation);
                    }
                }

                if (nextButton.getVisibility() == View.GONE) {
                    nextButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}