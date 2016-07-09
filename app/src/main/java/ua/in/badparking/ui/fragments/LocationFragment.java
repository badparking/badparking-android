package ua.in.badparking.ui.fragments;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.GeolocationService;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * @author Dima Kovalenko
 */
public class LocationFragment extends BaseFragment {

    private static final String TAG = LocationFragment.class.getName();


    private MapView mapView;

    private GoogleMap googleMap;
    private LatLng lastLatLng;
    private View mapHolder;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        mapHolder = rootView.findViewById(R.id.mapHolder);
        mapView = (MapView)rootView.findViewById(R.id.mvMap);
        mapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());

        googleMap = mapView.getMap();

        GeolocationService.INST.subscribe(new GeolocationService.ILocationListener() {
            @Override
            public void onLocationObtained(Location location) {
                mapHolder.setVisibility(View.VISIBLE);
                setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
        rootView.findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager locManager = (LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                Location location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(location != null) {
                    DecimalFormat df = new DecimalFormat("#.######");
                    ClaimState.INST.getClaim().setLatitude(df.format(location.getLatitude()).replace(",", "."));
                    ClaimState.INST.getClaim().setLongitude(df.format(location.getLongitude()).replace(",", "."));
                }
                ((MainActivity)getActivity()).moveToNext();
            }
        });

        setCenter(new LatLng(50.45, 30.523611));

        return rootView;
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void setCenter(final LatLng latLng) {
        Log.i(TAG, "Set camera center: lat - " + latLng.latitude + ", lng - " + latLng.longitude);
        lastLatLng = latLng;
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 19));
        } catch (NullPointerException e) {
            Log.e(TAG, "Exception setting map center", e);
        }
    }

}