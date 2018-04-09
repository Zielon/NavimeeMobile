package org.pl.android.drively.ui.finance.pages.daily.adapter;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.pages.daily.DailyMvpView;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DayFinanceAdapter extends RecyclerView.Adapter<DayFinanceAdapter.MyViewHolder> {

    @Getter
    @Setter
    private List<? extends Finance> finances;

    private DailyMvpView dailyMvpView;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView category;
        TextView description;
        TextView note;
        TextView amountLabel;
        ConstraintLayout itemLayout;

        MyViewHolder(View view) {
            super(view);
            category = (TextView) view.findViewById(R.id.category);
            description = (TextView) view.findViewById(R.id.description);
            note = (TextView) view.findViewById(R.id.note);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
            itemLayout = (ConstraintLayout) view.findViewById(R.id.item_layout);
        }
    }

    public DayFinanceAdapter(DailyMvpView dailyMvpView, List<? extends Finance> finances) {
        this.finances = finances;
        this.dailyMvpView = dailyMvpView;
    }

    @Override
    public DayFinanceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_finances_finance_row, parent, false);
        return new DayFinanceAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DayFinanceAdapter.MyViewHolder holder, int position) {
        Finance finance = finances.get(position);
        holder.category.setText(finance.getCategory());
        holder.description.setText(finance.getDescription());
        holder.note.setText(finance.getNote());
        holder.amountLabel.setText(finance.getAmount());
        holder.itemLayout.setOnClickListener(view -> dailyMvpView.startEditingFinance(finance));
    }

    @Override
    public int getItemCount() {
        return finances.size();
    }

}