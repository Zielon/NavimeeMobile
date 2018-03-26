package org.pl.android.drively.data.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Income extends Finance {

    @Builder
    public Income(String id, String amount, String description, String note,
                   String category, Date date, String attachmentPath) {
        super(id, amount, description, note, category, date, attachmentPath);
    }

}
