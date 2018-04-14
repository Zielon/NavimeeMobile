package org.pl.android.drively.data.model.eventbus;

import java.util.Date;

import lombok.Data;
import lombok.NonNull;

@Data
public class FinancePanelChange {

    @NonNull
    Date date;

}
