package org.pl.android.drively.ui.planner.dayschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;

import org.joda.time.DateTime;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.ui.planner.PlannerFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class DayScheduleFragment extends Fragment implements DayScheduleMvpView {

    @Inject
    DaySchedulePresenter mDaySchedulePresenter;

    @Inject
    DayScheduleAdapter mDayScheduleAdapter;

    @BindView(R.id.recycler_view_day_schedule)
    RecyclerView mDayScheduleRecycler;

    @BindView(R.id.icon_day_schedule)
    ImageView iconDaySchedule;

    @BindView(R.id.day_schedule_empty)
    RelativeLayout mDayScheduleEmptyLayout;

    Calendar selectedDate;
    SkeletonScreen skeletonScreen;
    HorizontalCalendar horizontalCalendar;

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

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_WEEK, 30);
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_WEEK, 0);

        if(PlannerFragment.selectedDate != null) {
            horizontalCalendar = new HorizontalCalendar.Builder(fragmentView, R.id.calendarView)
                    .range(startDate, endDate)
                    .datesNumberOnScreen(5)
                    .configure()// Number of Dates cells shown on screen (Recommended 5)
                        .textSize(15,15,15)
                        .formatMiddleText("EEE")      // WeekDay text format
                        .formatBottomText("dd")// Date format
                        .showTopText(true)
                        .showBottomText(true)
                    // Show or Hide month text
                    .end()
                    .defaultSelectedDate(PlannerFragment.selectedDate)  // Date to be seleceted at start (default to Today)
                    .build();
            selectedDate  = PlannerFragment.selectedDate;
        } else {
            horizontalCalendar = new HorizontalCalendar.Builder(fragmentView, R.id.calendarView)
                    .range(startDate, endDate)
                    .datesNumberOnScreen(5)
                    .configure()// Number of Dates cells shown on screen (Recommended 5)
                        .textSize(15,15,15)
                        .formatMiddleText("EEE")      // WeekDay text format
                        .formatBottomText("dd")// Date format
                        .showTopText(true)
                        .showBottomText(true)
                    // Show or Hide month text
                    .end()
                    .defaultSelectedDate(Calendar.getInstance())  // Date to be seleceted at start (default to Today)
                    .build();
            selectedDate  = Calendar.getInstance();
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
        mDaySchedulePresenter.attachView(this);
        mDayScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mDayScheduleRecycler.setHasFixedSize(true);
        mDayScheduleRecycler.setAdapter(mDayScheduleAdapter);

        skeletonScreen = Skeleton.bind(mDayScheduleRecycler)
                .adapter(mDayScheduleAdapter)
                .shimmer(true)
                .load(R.layout.item_skeleton_day_schedule)
                .color(R.color.primary_dark)
                .angle(10)
                .duration(1200)
                .count(1).show();

        queryOnDate(selectedDate);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDaySchedulePresenter.detachView();
    }

    private void queryOnDate(Calendar date) {
        PlannerFragment.selectedDate = date;
        mDayScheduleRecycler.setVisibility(View.VISIBLE);
        mDayScheduleEmptyLayout.setVisibility(View.GONE);
        skeletonScreen.show();
        mDaySchedulePresenter.loadEvents(date.getTime());
    }

    @Override
    public void showEvents(List<Event> events, DateTime dateTime) {
        if (events.size() == 0) {
            showEventsEmpty();
        } else {
            mDayScheduleAdapter.setEvents(events);
            mDayScheduleAdapter.setDateTime(dateTime);
            mDayScheduleAdapter.notifyDataSetChanged();
            skeletonScreen.hide();
        }
    }


    @Override
    public void showEventsEmpty() {
        mDayScheduleRecycler.animate().alpha(1.0f).setDuration(200);
        mDayScheduleRecycler.setVisibility(View.GONE);
        mDayScheduleEmptyLayout.setVisibility(View.VISIBLE);
        skeletonScreen.hide();
    }

    @OnClick(R.id.day_schedule_another_date)
    public void goToEventsFromLink(View view) {
        ((MainActivity) getActivity()).getBottomBar().selectTabWithId(R.id.tab_events);
    }


    @OnClick(R.id.icon_day_schedule)
    public void goToEventsFromIcon(View view) {
        ((MainActivity) getActivity()).getBottomBar().selectTabWithId(R.id.tab_events);
    }

    @Override
    public void showError() {
        skeletonScreen.hide();
    }

    @Override
    public void onSuccessDelete(Event event) {
        mDayScheduleAdapter.deleteEvent(event);
        mDayScheduleAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), getResources().getString(R.string.delete_day_schedule), Toast.LENGTH_SHORT).show();
    }
}
