package org.pl.android.drively.ui.finance.pages.monthly;

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
import org.pl.android.drively.ui.finance.pages.monthly.adapter.MonthAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import java8.util.Optional;

public class MonthlyFragment extends BaseFinanceFragment implements MonthlyMvpView {

    @Inject
    MonthlyPresenter monthlyPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.no_data_label)
    TextView noDataLabel;

    private MonthAdapter monthAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        monthAdapter = new MonthAdapter(new HashMap<>());
        View view = initializeRecyclerLayout(inflater, container);
        initializeRecyclerView();
        monthlyPresenter.attachView(this);
        monthlyPresenter.getYearFinances(((FinanceMvpView) getParentFragment()).getSelectedPanelDate());
        return view;
    }

    private void initializeRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(monthAdapter);
    }

    @Override
    public void onRefresh() {
        Optional.ofNullable(monthlyPresenter).ifPresent(monthlyPresenter -> monthlyPresenter.getYearFinances(((FinanceMvpView)getParentFragment()).getSelectedPanelDate()));
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        super.setSelectedPanelDate(calendar);
        Optional.ofNullable(monthlyPresenter).ifPresent(monthlyPresenter -> monthlyPresenter.getYearFinances(calendar));
    }

    @Override
    public void setData(Map<Integer, Double> monthFinances) {
        monthAdapter.setData(monthFinances);
        Optional.ofNullable(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> monthAdapter.notifyDataSetChanged()));
    }

    @Override
    public void hideProgressDialog() {
        super.hideProgressDialog();
        if (monthAdapter.getMonthlyFinances().isEmpty()) {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.VISIBLE)));
        } else {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.GONE)));
        }
    }

}
