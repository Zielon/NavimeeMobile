package org.pl.android.navimee.ui.dayschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.navimee.R;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class DayScheduleFragment extends Fragment {
    public static DayScheduleFragment newInstance() {
        DayScheduleFragment fragment = new DayScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.day_schedule_fragment, container, false);
    }
}
