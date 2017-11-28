package org.pl.android.navimee.ui.radar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.main.MainActivity;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class RadarFragment extends Fragment {
    public static RadarFragment newInstance() {
        RadarFragment fragment = new RadarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView text = (TextView) ((MainActivity) getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.app_bar_text);
        text.setText(getResources().getString(R.string.radar));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.radar_fragment, container, false);
    }
}