package ua.in.badparking.ui.fragments;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;

import ua.in.badparking.R;
import ua.in.badparking.model.Geolocation;
import ua.in.badparking.ui.MainActivity;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class PlaceFragment extends Fragment {

    private static final String TAG = "PlaceFragment";

    private Button bDefineAddress;
    private Button bDefineAddressGps;
    private Button bDefineAddressMap;

    private Geolocation geolocation;

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

        geolocation = new Geolocation(getActivity());

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
                geolocation.getLocation(LocationManager.GPS_PROVIDER, new AndroidDoneCallback<Location>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onDone(Location result) {
                        Log.i(TAG, "Location result - " + result);
                        Toast.makeText(getActivity(), "!CHANGE IT!, coordinates:\n Latitude - " + result.getLatitude() + "\n Longitude - " + result.getLongitude() , Toast.LENGTH_SHORT).show();

                    }
                }, new AndroidFailCallback<Throwable>() {
                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return AndroidExecutionScope.BACKGROUND;
                    }

                    @Override
                    public void onFail(Throwable result) {
                        Log.e(TAG, "Error getting location", result);
                        Toast.makeText(getActivity(), "Error message - " + result.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, null);
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