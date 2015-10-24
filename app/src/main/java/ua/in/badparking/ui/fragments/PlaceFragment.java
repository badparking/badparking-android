package ua.in.badparking.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.in.badparking.R;
import ua.in.badparking.data.TrespassController;
import ua.in.badparking.model.Geolocation;
import ua.in.badparking.ui.MainActivity;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class PlaceFragment extends Fragment {

    private static final String TAG = "PlaceFragment";

    private View positionButtonsLayout;
    private AutoCompleteTextView actvCities;
    private AutoCompleteTextView actvStreets;

    private ArrayAdapter<String> citiesAdapter;
    private ArrayAdapter<String> streetsAdapter;

    private Handler uiUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final Bundle data = msg.getData();

            Log.i(TAG, "Handler received data - " + data);

            if (data == null) {
                return false;
            } else {
                if (data.containsKey("city")) actvCities.setText(data.getString("city"));
                if (data.containsKey("street")) actvStreets.setText(data.getString("street"));

                return true;
            }
        }
    });

    private Geolocation geolocation;

    //    private MapView mapView;
//    private GoogleMap googleMap;
//    private Dialog mapDialog;
    private DialogFragment mapDialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place, container, false);
        rootView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).onSendClicked();
            }
        });

        positionButtonsLayout = rootView.findViewById(R.id.positionButtonsLayout);
        actvCities = ((AutoCompleteTextView)rootView.findViewById(R.id.city));
        actvStreets = ((AutoCompleteTextView)rootView.findViewById(R.id.address));

        citiesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        streetsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());

        actvCities.setAdapter(citiesAdapter);
        actvStreets.setAdapter(streetsAdapter);

        final String savedCity = TrespassController.INST.getTrespass().getCity();
        if (savedCity != null) {
            actvCities.setText(savedCity);
        }

        geolocation = new Geolocation(getActivity(), isGpsEnabled(), true, new Geolocation.UpdatedLocationCallback() {
            @Override
            public void locationUpdate(Location location) {
                Log.i(TAG, "New location - " + location);
            }
        }, new Geolocation.UpdateAddressesCallback() {
            @Override
            public void addressesUpdate(List<Address> addresses) {
                Log.i(TAG, "New addresses quantity - " + addresses.size());
                Log.i(TAG, "Addresses list - " + addresses);

                final Set<String> cities = new HashSet<>();
                final Set<String> streets = new HashSet<>();

                for (final Address address : addresses) {
                    if (address.getLocality() != null) cities.add(address.getLocality());
                    if (address.getThoroughfare() != null && address.getSubThoroughfare() != null)
                        streets.add(address.getThoroughfare() + "," + address.getSubThoroughfare());
                }

                Log.i(TAG, "Cities list - " + cities.toString());
                Log.i(TAG, "Streets list - " + streets.toString());

                citiesAdapter.clear();
                citiesAdapter.addAll(cities);

                final String defaultCity = cities.toArray()[0].toString();
                final String defaultStreet = streets.toArray()[0].toString();

                Log.i(TAG, "Default city - " + defaultCity);
                Log.i(TAG, "Default street - " + defaultStreet);

                streetsAdapter.clear();
                streetsAdapter.addAll(streets);

                final Message addressMessage = new Message();
                final Bundle addressData = new Bundle();
                addressData.putString("city", defaultCity);
                addressData.putString("street", defaultStreet);
                addressMessage.setData(addressData);

                uiUpdateHandler.sendMessage(addressMessage);
            }
        });

        Button bDefineAddress = (Button)rootView.findViewById(R.id.buttonDefineAddress);
        Button bDefineAddressGps = (Button)rootView.findViewById(R.id.buttonDefineGPS);
        Button bDefineAddressMap = (Button)rootView.findViewById(R.id.buttonDefineMap);

        bDefineAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityAndStreetLayout();
            }
        });

        bDefineAddressGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGpsEnabled()) {
                    geolocation.updateLocation();
                    geolocation.requestCurrentAddressesOptions(5);
                } else {
                    buildAlertMessage(getString(R.string.dialogGPSActivateQuestion));
                }
            }
        });

        bDefineAddressMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapDialogFragment.show(getFragmentManager(), "MapDialog");
            }
        });

//        mapDialog = new Dialog(getActivity());
//        mapDialog.setContentView(R.layout.map_dialog);
//
//        mapView = (MapView) mapDialog.findViewById(R.id.mvMap);
//        googleMap = mapView.getMap();

//        mapView = (MapView) rootView.findViewById();

        return rootView;
    }

    public void buildAlertMessage(String message) {
        if (getActivity().isFinishing())
            return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setCancelable(false).setPositiveButton("Так", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton("Hi", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showCityAndStreetLayout() {
        actvCities.setVisibility(View.VISIBLE);
        actvStreets.setVisibility(View.VISIBLE);
        actvCities.requestFocus();
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)positionButtonsLayout.getLayoutParams();
        layoutParams.bottomMargin = 300;// TODO dp
        positionButtonsLayout.requestLayout();
        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(actvCities.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceFragment newInstance() {
        return new PlaceFragment();
    }

    public static class MapDialog extends DialogFragment {

        private MapView mapView;
        private GoogleMap googleMap;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle bundle) {
            final View result = inflater.inflate(R.layout.map_dialog, container, false);

            mapView = (MapView)result.findViewById(R.id.mvMap);
            googleMap = mapView.getMap();

            return result;
        }

        public void setCenter(final LatLng latLng) {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            googleMap.animateCamera(cameraUpdate);
        }

        public LatLng getCenter() {
            return googleMap.getCameraPosition().target;
        }


    }
}