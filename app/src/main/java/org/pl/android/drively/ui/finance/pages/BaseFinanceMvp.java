package org.pl.android.drively.ui.finance.pages;

import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.progress.BaseProgressMvp;

import java.util.Calendar;
import java.util.List;

public interface BaseFinanceMvp extends BaseProgressMvp {
    void addAlreadyLoadedData(List<? extends Finance> finances);

    void setSelectedPanelDate(Calendar calendar);

    void setPanelAmount(String amount);
}
