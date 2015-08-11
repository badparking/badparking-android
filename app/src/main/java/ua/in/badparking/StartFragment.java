package ua.in.badparking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class StartFragment extends Fragment {

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);
        rootView.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).scrollToPlace();
            }
        });
        return rootView;
    }
}