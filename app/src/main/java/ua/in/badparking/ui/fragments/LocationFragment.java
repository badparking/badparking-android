package ua.in.badparking.ui.fragments;

import android.location.Address;
import android.location.Location;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.tajchert.sample.DotsTextView;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.GeolocationState;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * @author Dima Kovalenko & Vladimir Dranik
 */
public class LocationFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = LocationFragment.class.getName();

    private GoogleMap mMap;
    @BindView(R.id.dots)
    DotsTextView dotsTextView;
    @BindView(R.id.positioning_text_view)
    TextView positioningText;
    @BindView(R.id.next_button)
    Button nextButton;
    private Unbinder unbinder;
    private static boolean showHint = false;
    private static View rootView;

    public static Fragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_location, container, false);
        }

        unbinder = ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment fragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.mapFragment);
        fragment.getMapAsync(this);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).moveToNext();
            }
        });
        nextButton.setVisibility(View.GONE);

        if (GeolocationState.INST.isLocationActual()) {
            onLocationChanged(GeolocationState.INST.getLastLocation());
        }
    }

    @Subscribe
    public void onLocationChanged(Location location) {
        if (location != null && mMap != null) {
            Address address = GeolocationState.INST.getAddress(location);

            if (address != null) {
                ClaimState.INST.getClaim().setCity(address.getLocality());
                ClaimState.INST.getClaim().setAddress(address.getAddressLine(0));

                dotsTextView.hideAndStop();
                positioningText.setText(ClaimState.INST.getFullAddress());
                nextButton.setVisibility(View.VISIBLE);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                showTimePositioningHint();
            }

            DecimalFormat df = new DecimalFormat("#.######");
            ClaimState.INST.getClaim().setLatitude(df.format(location.getLatitude()).replace(",", "."));
            ClaimState.INST.getClaim().setLongitude(df.format(location.getLongitude()).replace(",", "."));
            GeolocationState.INST.mapPositioning(mMap, location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void showTimePositioningHint() {
        if (!showHint) {
            Toast toast = Toast.makeText(getContext(), "Будь ласка, зачекайте для більш точного геопозиціювання", Toast.LENGTH_LONG);
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
}