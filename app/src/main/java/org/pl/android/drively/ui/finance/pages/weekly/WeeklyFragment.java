package org.pl.android.drively.ui.finance.pages.weekly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.finance.FinanceMvpView;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;
import org.pl.android.drively.ui.finance.pages.weekly.adapter.WeeklyAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import java8.util.Optional;

public class WeeklyFragment extends BaseFinanceFragment implements WeeklyMvpView {

    @Inject
    WeeklyPresenter weeklyPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.no_data_label)
    TextView noDataLabel;

    private WeeklyAdapter weeklyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = initializeRecyclerLayout(inflater, container);
        weeklyPresenter.attachView(this);
        initializeRecyclerView();
        weeklyPresenter.getYearFinances(((FinanceMvpView) getParentFragment()).getSelectedPanelDate());
        return view;
    }

    private void initializeRecyclerView() {
        weeklyAdapter = new WeeklyAdapter(new HashMap<>());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(weeklyAdapter);
    }

    @Override
    public void onRefresh() {
        Optional.ofNullable(weeklyPresenter).ifPresent(weeklyPresenter -> weeklyPresenter.getYearFinances(((FinanceMvpView) getParentFragment()).getSelectedPanelDate()));
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        super.setSelectedPanelDate(calendar);
        Optional.ofNullable(weeklyPresenter).ifPresent(weeklyPresenter -> weeklyPresenter.getYearFinances(calendar));
    }

    @Override
    public void setData(Map<String, Double> weeklyFinances) {
        weeklyAdapter.setData(weeklyFinances);
        Optional.ofNullable(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> weeklyAdapter.notifyDataSetChanged()));
    }

    @Override
    public void hideProgressDialog() {
        super.hideProgressDialog();
        if (weeklyAdapter.getWeeklyFinances().isEmpty()) {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.VISIBLE)));
        } else {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.GONE)));
        }
    }

}
