package org.pl.android.drively.util;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarUtil {

    public static final Calendar calendar = Calendar.getInstance();

    public static SimpleDateFormat getThreeLetterMonthAndFourDigitsYearFormat() {
        return new SimpleDateFormat("MMM YYYY", Locale.getDefault());
    }

    public static Date getFirstDayOfMonth(Calendar calendar) {
        return LocalDate.fromCalendarFields(calendar).withDayOfMonth(1).toDate();
    }

    public static Date getLastDayOfMonth(Calendar calendar) {
        return LocalDate.fromCalendarFields(calendar).dayOfMonth().withMaximumValue().toDate();
    }

    public static Date getFirstDayOfYear(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static Date getLastDayOfYear(Calendar calendar) {
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        return calendar.getTime();
    }

    public static Date determineFirstDayForWeeklyUpdates(Calendar calendarWithExactMonth) {
        calendarWithExactMonth.set(GregorianCalendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendarWithExactMonth.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 1);
        if (calendarWithExactMonth.getActualMinimum(Calendar.DAY_OF_MONTH) != calendarWithExactMonth.get(Calendar.DAY_OF_MONTH)) {
            return LocalDate.fromCalendarFields(calendarWithExactMonth).minusDays(7).toDate();
        }
        return calendarWithExactMonth.getTime();
    }

    public static Date determineLastDayForWeeklyUpdates(Calendar calendarWithExactMonth) {
        calendarWithExactMonth.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendarWithExactMonth.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, -1);
        if (calendarWithExactMonth.getActualMaximum(Calendar.DAY_OF_MONTH) != calendarWithExactMonth.get(Calendar.DAY_OF_MONTH)) {
            return LocalDate.fromCalendarFields(calendarWithExactMonth).plusDays(7).toDate();
        }
        return calendarWithExactMonth.getTime();
    }

    public static String determineWeekString(Date date, Date firstDay, Date lastDay) {
        SimpleDateFormat dayAndMonthFormatter = new SimpleDateFormat("dd.MM", Locale.getDefault());
        LocalDate firstDayLocalDate = LocalDate.fromDateFields(firstDay);
        LocalDate rangeEnd = LocalDate.fromDateFields(firstDay).plusDays(7);
        while (!firstDayLocalDate.isEqual(LocalDate.fromDateFields(lastDay))) {
            if (isBetween(LocalDate.fromDateFields(date), firstDayLocalDate, rangeEnd)) {
                return dayAndMonthFormatter.format(firstDayLocalDate.toDate())
                        + " ~ "
                        + dayAndMonthFormatter.format(rangeEnd.toDate());
            } else {
                firstDayLocalDate = rangeEnd;
                rangeEnd = rangeEnd.plusDays(7);
            }
        }
        return "";
    }

    private static boolean isBetween(LocalDate date, LocalDate firstDay, LocalDate lastDay) {
        return firstDay.minusDays(1).isBefore(date) && lastDay.plusDays(1).isAfter(date);
    }

}
