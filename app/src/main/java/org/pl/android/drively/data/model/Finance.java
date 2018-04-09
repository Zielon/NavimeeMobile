package org.pl.android.drively.data.model;

import org.pl.android.drively.util.DoubleUtil;

import java.util.Date;
import java.util.HashMap;
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

    public Map<String, java.lang.Object> toMap() {
        Map<String, java.lang.Object> financeData = new HashMap<>();
        financeData.put("id", id);
        financeData.put("amount", amount.toString());
        financeData.put("description", description);
        financeData.put("note", note);
        financeData.put("category", category);
        financeData.put("date", date);
        financeData.put("attachmentPath", attachmentPath);
        return financeData;
    }

    public Double getAmountWithoutCurrency() {
        return DoubleUtil.getDoubleFromAmountWithCurrency(amount);
    }


}
