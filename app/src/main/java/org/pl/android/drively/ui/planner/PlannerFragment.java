package org.pl.android.drively.ui.planner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.ui.planner.dayschedule.DayScheduleFragment;
import org.pl.android.drively.ui.planner.events.EventsFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Wojtek on 2018-02-19.
 */

public class PlannerFragment  extends Fragment {


    public static String STR_CALENDAR_FRAGMENT = "CALENDAR";
    public static String STR_YOUR_PLAN_FRAGMENT = "YOUR_PLAN";

    @BindView(R.id.tab_layout_planner)
    TabLayout tabLayout;
    @BindView(R.id.viewpager_planner)
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    public static Calendar selectedDate;
    int[] tabIcons = {
            R.drawable.ic_action_today,
            R.drawable.ic_action_today
    };

    public static PlannerFragment newInstance() {
        PlannerFragment fragment = new PlannerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //((BaseActivity) getActivity()).activityComponent().inject(this);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && actionBar.getCustomView() != null) {
            TextView text = (TextView) actionBar.getCustomView().findViewById(R.id.app_bar_text);
            text.setText(getResources().getString(R.string.events));
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.planner_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        initTab();
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mChatPresenter.detachView();
    }


    private void initTab() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(this.getFragmentManager());
        adapter.addFrag(new EventsFragment(),STR_CALENDAR_FRAGMENT);
        adapter.addFrag(new DayScheduleFragment(), STR_YOUR_PLAN_FRAGMENT);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (!(adapter == null)) {
                    adapter.notifyDataSetChanged();
                    setupTabIcons();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupTabIcons() {

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }


        // This is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return POSITION_NONE;
        }
    }
}

