package org.pl.android.drively.ui.finance.pages;

import org.pl.android.drively.ui.finance.pages.daily.DailyFragment;
import org.pl.android.drively.ui.finance.pages.weekly.WeeklyFragment;
import org.pl.android.drively.util.CalendarUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TopLeftPanelHelper {

    public static String determineLeftPanelLabel(BaseFinanceFragment selectedFragment, Date newDate) {
        if (selectedFragment instanceof DailyFragment || selectedFragment instanceof WeeklyFragment) {
            return CalendarUtil.getThreeLetterMonthAndFourDigitsYearFormat().format(newDate);
        } else {
            return new SimpleDateFormat("yyyy", Locale.getDefault()).format(newDate);
        }
    }

    public static int determineDateElementToChange(BaseFinanceFragment selectedFragment) {
        if (selectedFragment instanceof DailyFragment || selectedFragment instanceof WeeklyFragment) {
            return Calendar.MONTH;
        } else {
            return Calendar.YEAR;
        }
    }
}
