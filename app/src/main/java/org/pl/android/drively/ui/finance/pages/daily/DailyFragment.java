package org.pl.android.drively.ui.finance.pages.daily;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.finance.FinanceMvpView;
import org.pl.android.drively.ui.finance.form.edit.EditFinanceActivity;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;
import org.pl.android.drively.ui.finance.pages.daily.adapter.DayAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import java8.util.Optional;

public class DailyFragment extends BaseFinanceFragment implements DailyMvpView {

    @Inject
    DailyPresenter dailyPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.no_data_label)
    TextView noDataLabel;

    private DayAdapter dayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dayAdapter = new DayAdapter(this, new HashMap<>());
        View view = initializeRecyclerLayout(inflater, container);
        initializeRecyclerView();
        dailyPresenter.attachView(this);
        dailyPresenter.getMonthFinances(((FinanceMvpView) getParentFragment()).getSelectedPanelDate());
        return view;
    }

    private void initializeRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(dayAdapter);
    }

    @Override
    public void setData(Map<Date, List<? extends Finance>> dailyFinances) {
        dayAdapter.setData(dailyFinances);
        Optional.ofNullable(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> dayAdapter.notifyDataSetChanged()));
    }

    @Override
    public void onRefresh() {
        Optional.ofNullable(dailyPresenter).ifPresent(dailyPresenter -> dailyPresenter.getMonthFinances(((FinanceMvpView)getParentFragment()).getSelectedPanelDate()));
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        super.setSelectedPanelDate(calendar);
        Optional.ofNullable(dailyPresenter).ifPresent(dailyPresenter -> dailyPresenter.getMonthFinances(calendar));
    }

    @Override
    public void hideRefreshing() {
        super.hideRefreshing();
        if (dayAdapter.getDailyFinances().isEmpty()) {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.VISIBLE)));
        } else {
            Optional.of(getActivity()).ifPresent(activity -> activity.runOnUiThread(() -> noDataLabel.setVisibility(View.GONE)));
        }
    }

    @Override
    public void startEditingFinance(Finance finance) {
        Intent intent = new Intent(getActivity(), EditFinanceActivity.class);
        intent.putExtra(EditFinanceActivity.FINANCE_INTENT, new Gson().toJson(finance));
        intent.putExtra(EditFinanceActivity.FINANCE_TYPE, finance instanceof Expense ? Expense.class.getName() : Income.class.getName());
        startActivity(intent);
    }
}
