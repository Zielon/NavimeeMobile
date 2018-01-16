package org.pl.android.drively.ui.dayschedule;

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

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;

/**
 * Created by Wojtek on 2017-10-21.
 */

public class DayScheduleFragment extends Fragment implements DayScheduleMvpView {

    @Inject
    DaySchedulePresenter mDaySchedulePresenter;

    @Inject DayScheduleAdapter mDayScheduleAdapter;

    @BindView(R.id.recycler_view_day_schedule)
    RecyclerView mDayScheduleRecycler;

    @BindView(R.id.day_schedule_empty)
    RelativeLayout mDayScheduleEmptyLayout;

    Date today;
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
        TextView text = (TextView) ((MainActivity) getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.app_bar_text);
        text.setText(getResources().getString(R.string.day_schedule));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.day_schedule_fragment, container, false);

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
                mDayScheduleRecycler.setVisibility(View.VISIBLE);
                mDayScheduleEmptyLayout.setVisibility(View.GONE);
                skeletonScreen.show();
                mDaySchedulePresenter.loadEvents(date);
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
        mDaySchedulePresenter.attachView(this);
        mDayScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mDayScheduleRecycler.setHasFixedSize(true);
        mDayScheduleRecycler.setAdapter(mDayScheduleAdapter);

        skeletonScreen = Skeleton.bind(mDayScheduleRecycler)
                .adapter(mDayScheduleAdapter)
                .shimmer(true)
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
        mDaySchedulePresenter.detachView();
    }

    @Override
    public void showEvents(List<Event> events) {
        mDayScheduleAdapter.setEvents(events);
        mDayScheduleAdapter.notifyDataSetChanged();
        skeletonScreen.hide();
    }


    @Override
    public void showEventsEmpty() {
        mDayScheduleRecycler.setVisibility(View.GONE);
        mDayScheduleEmptyLayout.setVisibility(View.VISIBLE);
        skeletonScreen.hide();
    }

    @OnClick(R.id.day_schedule_another_date)
    public void goToEvents(View view) {
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
        Toast.makeText(getActivity(),getResources().getString(R.string.delete_day_schedule), Toast.LENGTH_SHORT).show();
    }
}
