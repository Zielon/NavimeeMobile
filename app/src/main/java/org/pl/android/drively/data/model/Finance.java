package org.pl.android.drively.data.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Finance {

    private String id;

    private BigDecimal amount;

    private String category;

}
