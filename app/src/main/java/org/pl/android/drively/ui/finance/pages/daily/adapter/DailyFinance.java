package org.pl.android.drively.ui.finance.pages.daily.adapter;

import org.pl.android.drively.data.model.Finance;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class DailyFinance {

    private Date date;

    private List<? extends Finance> finances;
}
