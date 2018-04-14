package org.pl.android.drively.ui.finance.pages;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.progress.BaseProgressFragment;
import org.pl.android.drively.ui.finance.FinanceMvpView;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;

public class BaseFinanceFragment extends BaseProgressFragment implements BaseFinanceMvp {

    protected View initializeRecyclerLayout(LayoutInflater inflater, @Nullable ViewGroup container) {
        View fragmentView = inflater.inflate(R.layout.finances_recycler_view, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void addAlreadyLoadedData(List<? extends Finance> finances) {
        ((FinanceMvpView) getParentFragment()).getAlreadyLoadedData().addAll(finances);
    }

    @Override
    public void setSelectedPanelDate(Calendar calendar) {
        showProgressDialog(R.string.loading_finances);
    }

    @Override
    public void setPanelAmount(String amount) {
        ((FinanceMvpView) getParentFragment()).setPanelAmount(amount);
    }
}
