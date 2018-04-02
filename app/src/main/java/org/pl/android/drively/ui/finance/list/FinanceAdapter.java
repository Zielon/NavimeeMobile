package org.pl.android.drively.ui.finance.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.ViewHolder> {

    @Getter
    @Setter
    private List<? extends Finance> finances;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView amountLabel, categoryLabel;

        public ViewHolder(View view) {
            super(view);
            amountLabel = (TextView) view.findViewById(R.id.amount_label);
            categoryLabel = (TextView) view.findViewById(R.id.category_label);
        }
    }


    public FinanceAdapter(List<? extends Finance> finances) {
        this.finances = finances;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.finance_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Finance finance = finances.get(position);
        holder.amountLabel.setText(String.valueOf(finance.getAmount()));
        holder.categoryLabel.setText(finance.getCategory());
    }

    @Override
    public int getItemCount() {
        return finances.size();
    }

}
