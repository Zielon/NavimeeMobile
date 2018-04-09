package org.pl.android.drively.common;

import android.app.FragmentManager;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimePickerHelper {

    private static final String DATEPICKER_TAG = "DATEPICKER_TAG";

    private static final String TIMEPICKER_TAG = "TIMEPICKER_TAG";

    public static void showDatePicker(DatePickerDialog.OnDateSetListener listener, FragmentManager fragmentManager, int color) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                listener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.setAccentColor(color);
        datePicker.setVersion(DatePickerDialog.Version.VERSION_2);
        datePicker.show(fragmentManager, DATEPICKER_TAG);
    }

    public static void showTimePicker(TimePickerDialog.OnTimeSetListener listener, FragmentManager fragmentManager, int color) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePicker = TimePickerDialog.newInstance(
                listener,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true
        );
        timePicker.setVersion(TimePickerDialog.Version.VERSION_2);
        timePicker.setAccentColor(color);
        timePicker.show(fragmentManager, TIMEPICKER_TAG);
    }

    public static String getStringFromDate(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
        return sdfDate.format(date);
    }

    public static Date getDateFromString(String dateString) throws ParseException {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
        return sdfDate.parse(dateString);
    }

}
