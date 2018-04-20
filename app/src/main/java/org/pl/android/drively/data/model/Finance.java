package org.pl.android.drively.data.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.pl.android.drively.util.DoubleUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import timber.log.Timber;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Finance {

    private String id;

    private String amount;

    private String description;

    private String note;

    private String category;

    private Date date;

    private String attachmentPath;

    public Map toMap() {
        Map convertedMap = new ObjectMapper().convertValue(this, Map.class);
        convertedMap.put("date", date);
        return convertedMap;
    }

    public Double getAmountWithoutCurrency() {
        try {
            return DoubleUtil.getDoubleFromAmountWithCurrency(amount);
        } catch (ParseException e) {
            Timber.d("Parsing failed", e);
            return 0d;
        }
    }
}
