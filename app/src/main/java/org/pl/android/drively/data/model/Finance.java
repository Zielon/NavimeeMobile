package org.pl.android.drively.data.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.pl.android.drively.util.DoubleUtil;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        return DoubleUtil.getDoubleFromAmountWithCurrency(amount);
    }
}
