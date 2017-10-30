package org.pl.android.navimee.ui.dayschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class DayScheduleFragment extends Fragment implements DayScheduleMvpView {

    @Inject
    DaySchedulePresenter mDaySchedulePresenter;

    @Inject DayScheduleAdapter mDayScheduleAdapter;

    @BindView(R.id.recycler_view_day_schedule)
    RecyclerView mDayScheduleRecycler;
    
    
    public static DayScheduleFragment newInstance() {
        DayScheduleFragment fragment = new DayScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.day_schedule_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        mDaySchedulePresenter.attachView(this);
        mDayScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mDayScheduleRecycler.setHasFixedSize(true);
        mDayScheduleRecycler.setAdapter(mDayScheduleAdapter);
        return fragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();
        mDaySchedulePresenter.loadEvents();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDaySchedulePresenter.detachView();
    }

    @Override
    public void showEvents(List<Event> events) {
        mDayScheduleAdapter.setEvents(events);
        mDayScheduleAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEventsEmpty() {

    }

    @Override
    public void showError() {

    }

    @Override
    public void onSuccessDelete() {
        Toast.makeText(getActivity(),getResources().getString(R.string.delete_day_schedule), Toast.LENGTH_SHORT).show();
    }
}
