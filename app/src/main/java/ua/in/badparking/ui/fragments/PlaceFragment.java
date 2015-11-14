package ua.in.badparking.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
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
    private TextView topTextPlace;

    private ArrayAdapter<String> citiesAdapter;
    private ArrayAdapter<String> streetsAdapter;

    private Handler uiUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final Bundle data = msg.getData();
            if (data == null) {
                ((MainActivity)getActivity()).handleResult(8001, "Вибачте, ми не змогли визначити мiсто або вулицю.");
            }

            Log.i(TAG, "Handler received data - " + data);

            if (data == null) {
                return false;
            } else {
                showCityAndStreetLayout(false);
                topTextPlace.setText("Адресу визначено ....\n Натиснiть \"Вiдiслати\"");
                topTextPlace.setVisibility(View.VISIBLE);
                if (data.containsKey("city") && data.containsKey("street")) {
                    final String city = data.getString("city");
                    actvCities.setText(city);
                    String street = data.getString("street");
                    if (street.endsWith(",")) {
                        street = street.substring(0, street.length() - 1);
                    }
                    actvStreets.setText(street);
                } else {
                    ((MainActivity)getActivity()).handleResult(8001, "Вибачте, ми не змогли визначити мiсто або вулицю.");
                }

                return true;
            }
        }
    });

    private Button bDefineAddress;
    private Button bDefineAddressGps;
    private Button bDefineAddressMap;

    private Geolocation geolocation;
    private MapDialogFragment mapDialogFragment;

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
        topTextPlace = ((TextView)rootView.findViewById(R.id.topTextPlace));

        citiesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        streetsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());

        actvCities.setAdapter(citiesAdapter);
        actvStreets.setAdapter(streetsAdapter);

        final String savedCity = TrespassController.INST.getTrespass().getCity();
        if (savedCity != null) {
            actvCities.setText(savedCity);
        }

        mapDialogFragment = new MapDialogFragment().setMapPositionSetCallback(new MapDialogFragment.MapPositionSetCallback() {
            @Override
            public void receivePositionFromMap(CameraPosition cameraPosition) {
                geolocation.requestPositionAddressesOptions(cameraPosition.target, 5);
            }
        });

        geolocation = new Geolocation(getActivity(), true, true, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                final Bundle data = msg.getData();

                if (data == null) return false;
                else {
                    switch (msg.what) {
                        case 1: {
                            final List<Address> addresses = data.getParcelableArrayList("addresses");
                            if (addresses == null || addresses.isEmpty()) {
                                Log.e(TAG, "No addresses array");
                                uiUpdateHandler.sendMessage(new Message());
                            } else {
                                Log.i(TAG, "New addresses quantity - " + addresses.size());
                                Log.i(TAG, "Addresses list - " + addresses);

                                final Set<String> cities = new HashSet<>();
                                final Set<String> streets = new HashSet<>();

                                for (final Address address : addresses) {
                                    if (address.getLocality() != null) {
                                        cities.add(address.getLocality());
                                    }
                                    if (address.getThoroughfare() != null && address.getSubThoroughfare() != null) {
                                        streets.add(address.getThoroughfare() + "," + address.getSubThoroughfare());
                                    }
                                }

                                Log.i(TAG, "Cities list - " + cities.toString());
                                Log.i(TAG, "Streets list - " + streets.toString());

                                citiesAdapter.clear();
                                citiesAdapter.addAll(cities);

                                String defaultCity = null;
                                String defaultStreet = null;
                                final Message addressMessage = new Message();
                                final Bundle addressData = new Bundle();
                                if (cities.size() > 0 && streets.size() > 0) {
                                    defaultCity = cities.toArray()[0].toString();
                                    defaultStreet = streets.toArray()[0].toString();
                                    Log.i(TAG, "Default city - " + defaultCity);
                                    Log.i(TAG, "Default street - " + defaultStreet);

                                    addressData.putString("city", defaultCity);
                                    addressData.putString("street", defaultStreet);
                                }

                                streetsAdapter.clear();
                                streetsAdapter.addAll(streets);
                                addressMessage.setData(addressData);

                                uiUpdateHandler.sendMessage(addressMessage);
                            }
                            return true;
                        }
                        case 2: {
                            mapDialogFragment.setCenter(data.getDouble("Latitude"), data.getDouble("Longitude"));
                            return true;
                        }
                        default: {
                            Log.e(TAG, "Unknown request what sent to handler - " + msg.what);
                            return false;
                        }
                    }
                }
            }
        });

        bDefineAddress = (Button)rootView.findViewById(R.id.buttonDefineAddress);
        bDefineAddressGps = (Button)rootView.findViewById(R.id.buttonDefineGPS);
        bDefineAddressMap = (Button)rootView.findViewById(R.id.buttonDefineMap);

        bDefineAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityAndStreetLayout(true);
            }
        });

        bDefineAddressGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGpsEnabled()) {
                    topTextPlace.setVisibility(View.VISIBLE);
                    topTextPlace.setText("Визначаємо адресу....\nЗачекайте хвилинку будь-ласка");
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
                if (!((MainActivity)getActivity()).isOnline()) {
                    Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
                } else {
                    geolocation.updateLocation();
                    mapDialogFragment.show(getFragmentManager(), "mapDialog");
                }
            }
        });
        return rootView;
    }

    public boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showCityAndStreetLayout(boolean openKeyboard) {
        topTextPlace.setVisibility(View.GONE);
        actvCities.setVisibility(View.VISIBLE);
        actvStreets.setVisibility(View.VISIBLE);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)positionButtonsLayout.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.place_buttons_margin_bottom);
        if (openKeyboard) {
            actvCities.requestFocus();
            positionButtonsLayout.requestLayout();
            InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(actvCities.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceFragment newInstance() {
        return new PlaceFragment();
    }

    public static class MapDialogFragment extends DialogFragment {

        private MapView mapView;
        private GoogleMap googleMap;
        private MapPositionSetCallback mapPositionSetCallback;
        private CameraUpdate lastCameraUpdate;
        private LatLng lastLatLng;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle bundle) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            final View result = inflater.inflate(R.layout.map_dialog, container, false);

            mapView = (MapView)result.findViewById(R.id.mvMap);
            mapView.onCreate(bundle);
            MapsInitializer.initialize(getActivity());

            googleMap = mapView.getMap();

            if (lastCameraUpdate == null) {
                lastCameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(50, 30), 19);
            }

            final DialogFragment currentFragment = this;

            result.findViewById(R.id.bCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentFragment.dismiss();
                }
            });
            result.findViewById(R.id.bChoose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CameraPosition cameraPosition = googleMap.getCameraPosition();
                    if (mapPositionSetCallback == null) {
                        Log.e(TAG, "No MapPositionSetCallback defined");
                    } else {
                        mapPositionSetCallback.receivePositionFromMap(cameraPosition);
                    }

                    currentFragment.dismiss();
                }
            });

            return result;
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null) {

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setLayout(width, height);
            }
        }

        @Override
        public void onResume() {
            mapView.onResume();
            super.onResume();

            if (lastLatLng != null)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 10));
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

        public void setCenter(final double lat, final double lng) {
            setCenter(new LatLng(lat, lng));
        }

        public LatLng getCenter() {
            return googleMap.getCameraPosition().target;
        }

        public MapDialogFragment setMapPositionSetCallback(final MapPositionSetCallback mapPositionSetCallback) {

            this.mapPositionSetCallback = mapPositionSetCallback;
            return this;
        }

        public interface MapPositionSetCallback {

            void receivePositionFromMap(final CameraPosition cameraPosition);
        }
    }
}