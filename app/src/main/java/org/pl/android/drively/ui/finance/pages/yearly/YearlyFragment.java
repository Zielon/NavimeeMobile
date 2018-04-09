package org.pl.android.drively.ui.finance.pages.yearly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.finance.FinanceMvpView;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;
import org.pl.android.drively.ui.finance.pages.yearly.adapter.YearAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import java8.util.Optional;

public class YearlyFragment extends BaseFinanceFragment implements YearlyMvpView {

    @Inject
    YearlyPresenter yearlyPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private YearAdapter yearAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = initializeRecyclerLayout(inflater, container);
        yearlyPresenter.attachView(this);
        initializeRecyclerView();
        yearlyPresenter.getYearFinances(((FinanceMvpView) getParentFragment()).getSelectedPanelDate());
        return view;
    }

    private void initializeRecyclerView() {
        yearAdapter = new YearAdapter(new HashMap<>());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(yearAdapter);
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        Optional.ofNullable(yearlyPresenter).ifPresent(yearlyPresenter -> yearlyPresenter.getYearFinances(calendar));
    }

    @Override
    public void setData(Map<String, Double> yearlyFinances) {
        yearAdapter.setData(yearlyFinances);
        getActivity().runOnUiThread(() -> yearAdapter.notifyDataSetChanged());
    }

}
