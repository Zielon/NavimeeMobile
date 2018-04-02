package org.pl.android.drively.ui.finance.pages.daily;

import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.pages.BaseFinanceMvp;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DailyMvpView extends BaseFinanceMvp {
    void setData(Map<Date, List<? extends Finance>> dailyFinances);
    void startEditingFinance(Finance finance);
}
