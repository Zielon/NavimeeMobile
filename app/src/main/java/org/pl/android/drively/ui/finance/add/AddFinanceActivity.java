package org.pl.android.drively.ui.finance.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.rm.rmswitch.RMSwitch;
import com.tylersuehr.chips.ChipsInputLayout;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.data.model.chip.CategoryChip;
import org.pl.android.drively.ui.base.progress.BaseProgressActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFinanceActivity extends BaseProgressActivity implements AddFinanceMvpView {

    @Inject
    AddFinancePresenter addFinancePresenter;

    @BindView(R.id.finance_amount_input)
    EditText amoutnEditText;

    @BindView(R.id.finance_type_switch)
    RMSwitch financeTypeSwitch;

    @BindView(R.id.category_chip_input)
    ChipsInputLayout categoryChipInput;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, AddFinanceActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_finance);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        addFinancePresenter.attachView(this);
        initializeView();
    }

    private void initializeView() {
        addFinancePresenter.loadChips();
    }

    @Override
    public void setChips(List<CategoryChip> categoryChips) {
        categoryChipInput.setFilterableChipList(categoryChips);
    }

    @OnClick(R.id.add_finance_button)
    public void addFinanceButtonClick(View view) {
        addFinancePresenter.saveFinance(new Finance());
        showProgressDialog(R.string.finance_adding);
    }

    @OnClick(R.id.date_icon)
    public void dateIconClick(View view) {

    }

    @Override
    public void finishActivity() {
        finish();
    }
}
