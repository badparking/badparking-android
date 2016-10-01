package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.tajchert.sample.DotsTextView;
import ua.in.badparking.R;
import ua.in.badparking.events.LocationEvent;
import ua.in.badparking.services.ClaimService;
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
    private Marker userMarker;


    private static boolean showHint = false;
    private SupportMapFragment mMapFragment;

    public static BaseFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.framelayout_location_container, mMapFragment).commit();
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapFragment.getMapAsync(LocationFragment.this);
            }
        }, 700);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).showPage(MainActivity.PAGE_CLAIM_OVERVIEW);
            }
        });
        nextButton.setVisibility(View.GONE);
    }

    @Subscribe
    public void onEvent(LocationEvent locationEvent) {
        Location location = locationEvent.getLocation();

        if (userMarker == null && location != null && mMap != null) {
            Address address = GeolocationState.INST.getAddress(location.getLatitude(), location.getLongitude());
            setAddress(address);
//            GeolocationState.INST.getUserMarker().remove(); TODO vdranik can you take a look, looks like there is NP here <---
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
//        if (GeolocationState.INST.getLocation() != null) {
//            onEvent(new LocationEvent(GeolocationState.INST.getLocation()));
//        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

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

        if (GeolocationState.INST.getLocation() != null) {
            onEvent(new LocationEvent(GeolocationState.INST.getLocation()));
        }
    }

    public void mapPositioning(GoogleMap mMap, double latitude, double longitude) {
        if (userMarker != null) {
            userMarker.remove();
        }

        LatLng coordinates = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 17));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(17)
                    .bearing(45)
                    //.tilt(45)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude)));
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
            ClaimService.INST.getClaim().setCity(null);
            ClaimService.INST.getClaim().setAddress(null);

            dotsTextView.showAndPlay();
            positioningText.setText(getResources().getText(R.string.positioning_in_progress));
            nextButton.setVisibility(View.GONE);

        } else {
            ClaimService.INST.getClaim().setCity(address.getLocality());
            ClaimService.INST.getClaim().setAddress(address.getAddressLine(0));

            dotsTextView.hideAndStop();
            positioningText.setText(ClaimService.INST.getFullAddress());
            nextButton.setVisibility(View.VISIBLE);

            DecimalFormat df = new DecimalFormat("#.######");
            ClaimService.INST.getClaim().setLatitude(df.format(address.getLatitude()).replace(",", "."));
            ClaimService.INST.getClaim().setLongitude(df.format(address.getLongitude()).replace(",", "."));
            mapPositioning(mMap, address.getLatitude(), address.getLongitude());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        userMarker.remove();
        userMarker = null;
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