package org.pl.android.drively.ui.finance.pages.weekly;


import org.pl.android.drively.ui.finance.pages.BaseFinanceMvp;

import java.util.Map;

public interface WeeklyMvpView extends BaseFinanceMvp {
    void setData(Map<String, Double> weeklyFinances);
}
