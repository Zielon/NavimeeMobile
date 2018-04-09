package org.pl.android.drively.ui.finance.pages.daily.adapter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.ui.finance.pages.daily.DailyMvpView;
import org.pl.android.drively.util.DoubleUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lombok.Getter;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.MyViewHolder> {

    @Getter
    private Map<Date, List<? extends Finance>> dailyFinances;

    private List<Date> dateKeys;

    @ApplicationContext
    Context context;

    private DailyMvpView dailyMvpView;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumberLabel;
        TextView monthYearLabel;
        TextView dayNameLabel;
        TextView amountLabel;
        RecyclerView financesDetailRecyclerView;

        MyViewHolder(View view) {
            super(view);
            dayNumberLabel = (TextView) view.findViewById(R.id.day_number_label);
            monthYearLabel = (TextView) view.findViewById(R.id.month_year_label);
            dayNameLabel = (TextView) view.findViewById(R.id.day_name_label);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
            financesDetailRecyclerView = (RecyclerView) view.findViewById(R.id.finances_detail_recycler_view);
        }
    }

    public DayAdapter(DailyMvpView dailyMvpView, Map<Date, List<? extends Finance>> dailyFinances) {
        updateData(dailyFinances);
        this.dailyMvpView = dailyMvpView;
    }

    private void updateData(Map<Date, List<? extends Finance>> dailyFinances) {
        this.dailyFinances = dailyFinances;
        this.dateKeys = StreamSupport.stream(dailyFinances.entrySet())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setData(Map<Date, List<? extends Finance>> dailyFinances) {
        updateData(dailyFinances);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_finances_day_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Date date = dateKeys.get(position);
        List<? extends Finance> finances = dailyFinances.get(date);
        holder.dayNumberLabel.setText(String.valueOf(date.getDate()));
        holder.monthYearLabel.setText(new SimpleDateFormat("MM.YYYY").format(date));
        holder.dayNameLabel.setText(new SimpleDateFormat("EEEE").format(date));
        holder.amountLabel.setText(DoubleUtil.getStringWithCurrencyFromDouble(getFinancesSum(finances)));
        DayFinanceAdapter dayFinanceAdapter = new DayFinanceAdapter(dailyMvpView, finances);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        holder.financesDetailRecyclerView.setLayoutManager(mLayoutManager);
        holder.financesDetailRecyclerView.setItemAnimator(new DefaultItemAnimator());
        holder.financesDetailRecyclerView.setHasFixedSize(true);
        holder.financesDetailRecyclerView.setAdapter(dayFinanceAdapter);
    }

    @Override
    public int getItemCount() {
        return dailyFinances.size();
    }

    private Double getFinancesSum(List<? extends Finance> finances) {
        return StreamSupport.stream(finances)
                .mapToDouble(Finance::getAmountWithoutCurrency).sum();
    }
}
