package org.pl.android.navimee.ui.events;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.main.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class EventsFragment extends Fragment  implements EventsMvpView {

    @Inject EventsPresenter mEventsPresenter;

    @Inject EventsAdapter mEventsAdapter;

    @BindView(R.id.recycler_view_events)
    RecyclerView mEventsRecycler;

    @BindView(R.id.events_empty)
    RelativeLayout mEventsEmptyLayout;

    Date today;
    SkeletonScreen skeletonScreen;
    HorizontalCalendar horizontalCalendar;

    GeoFire geoFire;

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        TextView text = (TextView) ((MainActivity) getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.app_bar_text);
        text.setText(getResources().getString(R.string.events));
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.events_fragment, container, false);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_WEEK, 6);
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_WEEK, 0);

        today = Calendar.getInstance().getTime();

        horizontalCalendar = new HorizontalCalendar.Builder(fragmentView, R.id.calendarView)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .datesNumberOnScreen(5)   // Number of Dates cells shown on screen (Recommended 5)
                .dayNameFormat("EEE")	  // WeekDay text format
                .dayNumberFormat("dd")// Date format
                .monthFormat("MMM") 	  // Month format
                .showDayName(true)	  // Show or Hide dayName text
                .showMonthName(false)	  // Show or Hide month text
                .defaultSelectedDate(today)  // Date to be seleceted at start (default to Today)
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                mEventsRecycler.setVisibility(View.VISIBLE);
                mEventsEmptyLayout.setVisibility(View.GONE);
                skeletonScreen.show();
                mEventsAdapter.clearEvents();
                mEventsPresenter.clearEvents();
                double latitude = mEventsPresenter.getLastLat();
                double longitude = mEventsPresenter.getLastLng();
                geoFire.queryAtLocation(new GeoLocation(latitude, longitude),16).addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        Timber.i(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                       // mHotspotPresenter.loadHotSpotPlace(key);
                        mEventsPresenter.loadEvents(date,key);
                    }

                    @Override
                    public void onKeyExited(String key) {
                        Timber.i(String.format("Key %s is no longer in the search area", key));
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Timber.i(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                    }

                    @Override
                    public void onGeoQueryReady() {
                      //  skeletonScreen.hide();
                        Timber.i("All initial data has been loaded and events have been fired!");
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Timber.e("There was an error with this query: " + error);
                    }
                });
             //   mEventsPresenter.loadEvents(date);
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView,
                                         int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Date date, int position) {
                return true;
            }
        });

        ButterKnife.bind(this, fragmentView);

        mEventsPresenter.attachView(this);
        mEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventsRecycler.setHasFixedSize(true);
        mEventsRecycler.setAdapter(mEventsAdapter);
        mEventsPresenter.loadDayScheduleEvents();
        geoFire = new GeoFire(mEventsPresenter.getHotSpotDatabaseRefernce());

       skeletonScreen = Skeleton.bind(mEventsRecycler)
                .adapter(mEventsAdapter)
                .shimmer(true)
                .color(R.color.primary)
                .angle(20)
                .duration(1200)
                .count(10).show();
        return fragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEventsPresenter.detachView();
    }

    @OnClick(R.id.events_check_another_date)
    public void checkAnotherDate(View view) {
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(horizontalCalendar.getSelectedDate());
        c.add(Calendar.DATE, 1);
        dt = c.getTime();
        horizontalCalendar.selectDate(dt,true);
    }


    @Override
    public void showEventsEmpty() {
        mEventsRecycler.setVisibility(View.GONE);
        mEventsEmptyLayout.setVisibility(View.VISIBLE);
        skeletonScreen.hide();
    }

    @Override
    public void showError() {
        skeletonScreen.hide();
    }


    @Override
    public void onSuccessSave() {
        Toast.makeText(getActivity(),getResources().getString(R.string.save_day_schedule), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEvents(List<Event> eventsList) {
        mEventsAdapter.addEvents(eventsList);
        mEventsAdapter.notifyDataSetChanged();
        skeletonScreen.hide();
    }

}