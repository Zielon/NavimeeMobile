package org.pl.android.drively.ui.finance.pages;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.progress.BaseProgressFragment;
import org.pl.android.drively.ui.finance.FinanceMvpView;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.Optional;
import lombok.Getter;

public abstract class BaseFinanceFragment extends BaseProgressFragment implements BaseFinanceMvp, SwipeRefreshLayout.OnRefreshListener {

    @Getter
    @BindView(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    public abstract void onRefresh();

    protected View initializeRecyclerLayout(LayoutInflater inflater, @Nullable ViewGroup container) {
        View fragmentView = inflater.inflate(R.layout.finances_recycler_view, container, false);
        ButterKnife.bind(this, fragmentView);
        if (!((FinanceMvpView) getParentFragment()).isPopupShown()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        swipeRefreshLayout.setOnRefreshListener(this);
        return fragmentView;
    }

    @Override
    public void addAlreadyLoadedData(List<? extends Finance> finances) {
        ((FinanceMvpView) getParentFragment()).getAlreadyLoadedData().addAll(finances);
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        Optional.ofNullable(swipeRefreshLayout).ifPresent(swipeRefreshLayout1 -> swipeRefreshLayout1.setRefreshing(true));
    }

    @Override
    public void setPanelAmount(String amount) {
        ((FinanceMvpView) getParentFragment()).setPanelAmount(amount);
    }

    @Override
    public void showRefreshing() {
        Optional.ofNullable(getActivity())
                .ifPresent(activity -> activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true)));
    }

    @Override
    public void hideRefreshing() {
        Optional.ofNullable(getActivity())
                .ifPresent(activity -> activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false)));
    }
}
