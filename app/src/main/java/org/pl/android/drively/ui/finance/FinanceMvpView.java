package org.pl.android.drively.ui.finance;

import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.tab.TabMvpView;

import java.util.List;

public interface FinanceMvpView extends TabMvpView {

    void showNoDataLabel();

    void updateFinances(List<? extends Finance> finances);
}
