package org.pl.android.drively.ui.finance.pages.monthly;

import org.pl.android.drively.ui.finance.pages.BaseFinanceMvp;

import java.util.Map;

public interface MonthlyMvpView extends BaseFinanceMvp {
    void setData(Map<Integer, Double> monthFinances);
}
