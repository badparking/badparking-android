package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

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

    private Button bDefineAddress;
    private Button bDefineAddressGps;
    private Button bDefineAddressMap;

    private Geolocation geolocation;

//    private MapView mapView;
//    private GoogleMap googleMap;
//    private Dialog mapDialog;

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

        geolocation = new Geolocation(getActivity(), true, true, new Geolocation.UpdatedLocationCallback() {
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

        bDefineAddress = (Button)rootView.findViewById(R.id.buttonDefineAddress);
        bDefineAddressGps = (Button)rootView.findViewById(R.id.buttonDefineGPS);
        bDefineAddressMap = (Button)rootView.findViewById(R.id.buttonDefineMap);

        bDefineAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button button = (Button)v;

                if (button.getText().equals("Визначити адресу")) {

                    bDefineAddressGps.setVisibility(View.VISIBLE);
                    bDefineAddressMap.setVisibility(View.VISIBLE);
                    bDefineAddress.setVisibility(View.GONE);
                } else {
                    button.setText("Визначити адресу");

                    bDefineAddressGps.setVisibility(View.GONE);
                    bDefineAddressMap.setVisibility(View.GONE);
                }
            }
        });

        bDefineAddressGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geolocation.updateLocation();
                geolocation.requestCurrentAddressesOptions(5);
            }
        });

        bDefineAddressMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceFragment newInstance() {
        PlaceFragment fragment = new PlaceFragment();
        return fragment;
    }
}