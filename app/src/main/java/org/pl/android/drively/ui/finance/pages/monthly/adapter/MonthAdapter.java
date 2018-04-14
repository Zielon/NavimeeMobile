package org.pl.android.drively.ui.finance.pages.monthly.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.pl.android.drively.R;
import org.pl.android.drively.util.DoubleUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lombok.Getter;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MyViewHolder> {

    @Getter
    private Map<Integer, Double> monthlyFinances;

    private List<Integer> monthKeys;

    public MonthAdapter(Map<Integer, Double> finances) {
        this.updateData(finances);
    }

    private void updateData(Map<Integer, Double> monthlyFinances) {
        this.monthlyFinances = monthlyFinances;
        this.monthKeys = StreamSupport.stream(monthlyFinances.entrySet())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setData(Map<Integer, Double> dailyFinances) {
        updateData(dailyFinances);
    }

    @Override
    public MonthAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.finances_two_textview_row, parent, false);
        return new MonthAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MonthAdapter.MyViewHolder holder, int position) {
        Integer monthKey = monthKeys.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthKey);
        LocalDate localDate = new LocalDate();
        localDate = localDate.minusMonths(localDate.getMonthOfYear() - monthKey - 1);
        holder.infoLabel.setText(localDate.monthOfYear().getAsText());
        holder.amountLabel.setText(String.valueOf(DoubleUtil.getStringWithCurrencyFromDouble(monthlyFinances.get(monthKey))));
    }

    @Override
    public int getItemCount() {
        return monthlyFinances.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView infoLabel;
        TextView amountLabel;

        MyViewHolder(View view) {
            super(view);
            infoLabel = (TextView) view.findViewById(R.id.info_label);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
        }
    }

}