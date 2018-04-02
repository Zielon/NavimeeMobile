package org.pl.android.drively.ui.finance.pages.yearly.adapter;

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

public class YearAdapter extends RecyclerView.Adapter<YearAdapter.MyViewHolder> {

    @Getter
    private Map<String, Double> yearlyFinances;

    private List<String> categoryKeys;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView infoLabel;
        TextView amountLabel;

        MyViewHolder(View view) {
            super(view);
            infoLabel = (TextView) view.findViewById(R.id.info_label);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
        }
    }

    public YearAdapter(Map<String, Double> finances) {
        this.updateData(finances);
    }

    private void updateData(Map<String, Double> monthlyFinances) {
        this.yearlyFinances = monthlyFinances;
        this.categoryKeys = StreamSupport.stream(monthlyFinances.entrySet())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setData(Map<String, Double> dailyFinances) {
        updateData(dailyFinances);
    }

    @Override
    public YearAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.finances_two_textview_row, parent, false);
        return new YearAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(YearAdapter.MyViewHolder holder, int position) {
        String categoryKey = categoryKeys.get(position);
        holder.infoLabel.setText(categoryKey);
        holder.amountLabel.setText(DoubleUtil.getStringWithCurrencyFromDouble(yearlyFinances.get(categoryKey)));
    }

    @Override
    public int getItemCount() {
        return yearlyFinances.size();
    }

}