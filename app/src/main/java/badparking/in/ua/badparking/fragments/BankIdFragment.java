package badparking.in.ua.badparking.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.ViewPager;
import android.widget.ImageButton;

import badparking.in.ua.badparking.R;

public class BankIdFragment extends Fragment {

    ViewPager viewPager;
    ImageButton leftNav;
    ImageButton rightNav;

    public BankIdFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bank_id, container, false);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        leftNav = (ImageButton) rootView.findViewById(R.id.left_nav);
        rightNav = (ImageButton) rootView.findViewById(R.id.right_nav);

        leftNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tab = viewPager.getCurrentItem();
                if (tab > 0) {
                    tab--;
                    viewPager.setCurrentItem(tab);
                } else if (tab == 0) {
                    viewPager.setCurrentItem(tab);
                }
            }
        });

        rightNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tab = viewPager.getCurrentItem();
                tab++;
                viewPager.setCurrentItem(tab);
            }
        });

        return inflater.inflate(R.layout.fragment_bank_id, container, false);
    }
}

