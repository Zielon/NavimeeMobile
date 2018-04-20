package org.pl.android.drively.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class DoubleUtil {

    public static Double getDoubleFromAmountWithCurrency(String amount) throws ParseException {
        return parse(amount, Locale.getDefault()).doubleValue();
    }

    public static String getStringWithCurrencyFromDouble(Double amount) {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount);
    }

    public static BigDecimal parse(final String amount, final Locale locale) throws ParseException {
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        if (format instanceof DecimalFormat) {
            ((DecimalFormat) format).setParseBigDecimal(true);
        }
        return (BigDecimal) format.parse(amount.replaceAll("[^\\d.,]", ""));
    }

}
