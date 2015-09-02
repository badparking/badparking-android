package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.in.badparking.R;
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


    private Button bDefineAddress;
    private Button bDefineAddressGps;
    private Button bDefineAddressMap;

    private Geolocation geolocation;
    private Toast toast;

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

        toast = Toast.makeText(getActivity(), "!CHANGE IT!, coordinates:\n Latitude - " , Toast.LENGTH_SHORT);

        actvCities = ((AutoCompleteTextView) rootView.findViewById(R.id.city));
        actvCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selected = parent.getAdapter().getItem(position).toString();

//                actvCities.setText(selected);
            }
        });
        actvStreets = ((AutoCompleteTextView) rootView.findViewById(R.id.address));

        citiesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        streetsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());

        actvCities.setAdapter(citiesAdapter);
        actvStreets.setAdapter(streetsAdapter);

        geolocation = new Geolocation(getActivity(), true, true, new Geolocation.UpdatedLocationCallback() {
            @Override
            public void locationUpdate(Location location) {
                Log.i(TAG, "New location - " + location);
                toast.setText("!CHANGE IT!, coordinates:\n Latitude - " + location.getLatitude() + "\n Longitude - " + location.getLongitude());
                toast.show();
            }
        }, new Geolocation.UpdateAddressesCallback() {
            @Override
            public void addressesUpdate(List<Address> addresses) {
                Log.i(TAG, "New addresses quantity - " + addresses.size());

                final Set<String> cities = new HashSet<>();
                final Set<String> streets = new HashSet<>();

                for (final Address address: addresses) {
                    if (address.getLocality() != null) cities.add(address.getLocality());
                    if (address.getThoroughfare() != null && address.getSubThoroughfare() != null) streets.add(address.getThoroughfare() + "," + address.getSubThoroughfare());
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

                actvCities.setText(defaultCity);
                actvStreets.setText(defaultStreet);

//                actvCities.refreshDrawableState();
//                actvStreets.refreshDrawableState();
             }
        });

        bDefineAddress = (Button) rootView.findViewById(R.id.buttonDefineAddress);
        bDefineAddressGps = (Button) rootView.findViewById(R.id.buttonDefineGPS);
        bDefineAddressMap = (Button) rootView.findViewById(R.id.buttonDefineMap);

        bDefineAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button button = (Button) v;

                if (button.getText().equals("Визначити адресу")) {
                    button.setText("Приховати");

                    bDefineAddressGps.setVisibility(View.VISIBLE);
                    bDefineAddressMap.setVisibility(View.VISIBLE);
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