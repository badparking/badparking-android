package ua.in.badparking;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.Collections;

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

        Resources res = getResources();
        String[] trespassTypes = res.getStringArray(R.array.trespass_types);
        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Arrays.asList(trespassTypes));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner)rootView.findViewById(R.id.trespassSpinner);
        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Оберiть");
        // выделяем элемент
        spinner.setSelection(2);
        // устанавливаем обработчик нажатия

        return rootView;
    }
}