package org.pl.android.drively.ui.finance.pages.weekly.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.util.DoubleUtil;

import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lombok.Getter;

public class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.MyViewHolder> {

    @Getter
    private Map<String, Double> weeklyFinances;

    private List<String> weeklyKeys;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView infoLabel;
        TextView amountLabel;

        MyViewHolder(View view) {
            super(view);
            infoLabel = (TextView) view.findViewById(R.id.info_label);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
        }
    }

    public WeeklyAdapter(Map<String, Double> finances) {
        this.updateData(finances);
    }

    private void updateData(Map<String, Double> monthlyFinances) {
        this.weeklyFinances = monthlyFinances;
        this.weeklyKeys = StreamSupport.stream(monthlyFinances.entrySet())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setData(Map<String, Double> dailyFinances) {
        updateData(dailyFinances);
    }

    @Override
    public WeeklyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.finances_two_textview_row, parent, false);
        return new WeeklyAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WeeklyAdapter.MyViewHolder holder, int position) {
        String weekKey = weeklyKeys.get(position);
        holder.infoLabel.setText(weekKey);
        holder.amountLabel.setText(String.valueOf(DoubleUtil.getStringWithCurrencyFromDouble(weeklyFinances.get(weekKey))));
    }

    @Override
    public int getItemCount() {
        return weeklyFinances.size();
    }

}