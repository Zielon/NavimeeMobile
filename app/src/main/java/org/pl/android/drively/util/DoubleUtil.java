package org.pl.android.drively.util;

import java.text.NumberFormat;
import java.util.Locale;

public class DoubleUtil {

    public static Double getDoubleFromAmountWithCurrency(String amount) {
        return Double.valueOf(amount.replace(",", ".")
                .replaceAll("[^0-9.]","")
        );
    }

    public static String getStringWithCurrencyFromDouble(Double amount) {
        return  NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount);
    }

}
