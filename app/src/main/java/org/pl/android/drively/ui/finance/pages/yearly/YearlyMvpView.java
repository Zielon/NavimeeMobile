package org.pl.android.drively.ui.finance.pages.yearly;

import org.pl.android.drively.ui.finance.pages.BaseFinanceMvp;

import java.util.Map;

public interface YearlyMvpView extends BaseFinanceMvp {
    void setData(Map<String, Double> yearlyFinances);
}
