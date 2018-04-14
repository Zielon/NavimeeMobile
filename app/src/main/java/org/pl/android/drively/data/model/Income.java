package org.pl.android.drively.data.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Income extends Finance {

    @Builder
    public Income(String id, String amount, String description, String note,
                  String category, Date date, String attachmentPath) {
        super(id, amount, description, note, category, date, attachmentPath);
    }

}
