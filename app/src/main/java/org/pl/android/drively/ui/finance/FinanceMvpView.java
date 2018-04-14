package org.pl.android.drively.ui.finance;

import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.tab.TabMvpView;

import java.util.Calendar;
import java.util.Set;

public interface FinanceMvpView extends TabMvpView {
    Calendar getSelectedPanelDate();

    void setPanelAmount(String amount);

    Set<Finance> getAlreadyLoadedData();
}
