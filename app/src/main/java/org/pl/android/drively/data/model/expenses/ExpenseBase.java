package org.pl.android.drively.data.model.expenses;

import java.util.Date;

public class ExpenseBase {
    private double price;
    private Date date;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
