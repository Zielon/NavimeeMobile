package org.pl.android.navimee.ui.events;

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
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class EventsFragment extends Fragment  implements EventsMvpView {

    @Inject EventsPresenter mEventsPresenter;

    @Inject EventsAdapter mEventsAdapter;

    @BindView(R.id.recycler_view_events)
    RecyclerView mEventsRecycler;

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
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

        View fragmentView = inflater.inflate(R.layout.events_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        mEventsPresenter.attachView(this);
        mEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventsRecycler.setHasFixedSize(true);
        mEventsRecycler.setAdapter(mEventsAdapter);
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventsPresenter.loadEvents();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEventsPresenter.detachView();
    }

    @Override
    public void showEvents(Map<String, Object> events) {
        mEventsAdapter.setEvents(events);
        mEventsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEventsEmpty() {

    }

    @Override
    public void showError() {

    }

    @Override
    public void onSuccessSave() {
        Toast.makeText(getActivity(),getResources().getString(R.string.save_day_schedule), Toast.LENGTH_SHORT).show();
    }
}