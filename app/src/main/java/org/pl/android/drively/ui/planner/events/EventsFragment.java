package org.pl.android.drively.ui.planner.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.joda.time.DateTime;
import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.planner.PlannerFragment;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class EventsFragment extends BaseTabFragment implements EventsMvpView {

    @Inject
    EventsPresenter mEventsPresenter;

    @Inject
    DataManager dataManager;

    @Inject
    EventsAdapter mEventsAdapter;

    @BindView(R.id.recycler_view_events)
    RecyclerView mEventsRecycler;

    @BindView(R.id.events_empty)
    RelativeLayout mEventsEmptyLayout;

    Calendar selectedDate;
    SkeletonScreen skeletonScreen;
    HorizontalCalendar horizontalCalendar;
    ObjectMapper mapper = new ObjectMapper();
    GeoFire geoFire;
    GeoQuery geoQuery;

    private MaterialDialog popup;

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mEventsPresenter.attachView(this);

        View fragmentView = inflater.inflate(R.layout.events_fragment, container, false);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_WEEK, 30);
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_WEEK, 0);

        if (PlannerFragment.selectedDate != null) {
            horizontalCalendar = new HorizontalCalendar.Builder(fragmentView, R.id.calendarView)
                    .range(startDate, endDate)
                    .datesNumberOnScreen(5)
                    .configure()// Number of Dates cells shown on screen (Recommended 5)
                    .textSize(15, 15, 15)
                    .formatMiddleText("EEE")      // WeekDay text format
                    .formatBottomText("dd")// Date format
                    .showTopText(true)
                    .showBottomText(true)
                    // Show or Hide month text
                    .end()
                    .defaultSelectedDate(PlannerFragment.selectedDate)  // Date to be seleceted at start (default to Today)
                    .build();
            selectedDate = PlannerFragment.selectedDate;
        } else {
            horizontalCalendar = new HorizontalCalendar.Builder(fragmentView, R.id.calendarView)
                    .range(startDate, endDate)
                    .datesNumberOnScreen(5)
                    .configure()// Number of Dates cells shown on screen (Recommended 5)
                    .textSize(15, 15, 15)
                    .formatMiddleText("EEE")      // WeekDay text format
                    .formatBottomText("dd")// Date format
                    .showTopText(true)
                    .showBottomText(true)
                    // Show or Hide month text
                    .end()
                    .defaultSelectedDate(Calendar.getInstance())  // Date to be seleceted at start (default to Today)
                    .build();
            selectedDate = Calendar.getInstance();
        }


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                queryOnDate(date);
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView,
                                         int dx, int dy) {
            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

        ButterKnife.bind(this, fragmentView);

        mEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventsRecycler.setHasFixedSize(true);
        mEventsRecycler.setAdapter(mEventsAdapter);
        mEventsAdapter.setCallback(this::handleAdapterCallback);
        mEventsPresenter.loadDayScheduleEvents();
        geoFire = new GeoFire(mEventsPresenter.getHotSpotDatabaseRefernce());

        skeletonScreen = Skeleton.bind(mEventsRecycler)
                .adapter(mEventsAdapter)
                .shimmer(true)
                .load(R.layout.item_skeleton_event)
                .color(R.color.primary_dark)
                .angle(10)
                .duration(1200)
                .count(1).show();

        queryOnDate(selectedDate);

        return fragmentView;
    }

    private void handleAdapterCallback(EventsAdapter.EventsAdapterAction eventsAdapterAction, Object additionalData) {
        switch (eventsAdapterAction) {
            case SAVE: {
                mEventsPresenter.saveEvent((Event) additionalData);
                break;
            }
            case DELETE: {
                mEventsPresenter.deleteEvent((Event) additionalData);
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void updateDayScheduleListInAdapter(List<Event> dayScheduleList) {
        mEventsAdapter.setDayScheduleList(dayScheduleList);
    }

    private void queryOnDate(Calendar date) {
        mEventsRecycler.setVisibility(View.VISIBLE);
        mEventsEmptyLayout.setVisibility(View.GONE);
        skeletonScreen.show();
        mEventsAdapter.clearEvents();
        mEventsPresenter.clearEvents();
        PlannerFragment.selectedDate = date;
        double latitude = mEventsPresenter.getLastLat();
        double longitude = mEventsPresenter.getLastLng();

        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 10);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                Event event = mapper.convertValue(dataSnapshot.getValue(), Event.class);
                String uuid = event.getId() + "_" + dataManager.getPreferencesHelper().getUserId();
                event.setFirestoreId(uuid);
                mEventsPresenter.add(date.getTime(), event);
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                mEventsPresenter.show(date.getTime());
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEventsPresenter.detachView();
        geoQuery.removeAllListeners();
    }

    @OnClick(R.id.events_check_another_date)
    public void checkAnotherDateText(View view) {
        moveToNextDay();
    }

    @OnClick(R.id.icon_events)
    public void checkAnotherDateIcon(View view) {
        moveToNextDay();
    }

    private void moveToNextDay() {
        Calendar c = horizontalCalendar.getSelectedDate();
        c.add(Calendar.DATE, 1);
        horizontalCalendar.selectDate(c, true);
    }

    @Override
    public void showEventsEmpty() {
        mEventsRecycler.animate().alpha(1.0f).setDuration(200);
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
        Toast.makeText(getActivity(), getResources().getString(R.string.save_day_schedule), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEvents(List<Event> eventsList, DateTime dateTime) {
        if (eventsList.size() == 0) {
            showEventsEmpty();
        } else {
            mEventsRecycler.setVisibility(View.VISIBLE);
            mEventsEmptyLayout.setVisibility(View.GONE);
            mEventsAdapter.addEvents(eventsList);
            mEventsAdapter.setDateTime(dateTime);
            mEventsAdapter.notifyDataSetChanged();
            skeletonScreen.hide();
        }
    }

    @Override
    public void onSuccessDelete() {
        Toast.makeText(getActivity(), getResources().getString(R.string.delete_day_schedule), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showInstructionPopup() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.events_popup_instruction, null);
        preparePopupLayout(view);
        popup = new MaterialDialog.Builder(getActivity())
                .customView(view, false)
                .backgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent))
                .show();
    }

    private void preparePopupLayout(View rootView) {
        rootView.findViewById(R.id.understood_button).setOnClickListener(view -> popup.dismiss());
    }
}