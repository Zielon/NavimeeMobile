package org.pl.android.drively.ui.finance.add;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rm.rmswitch.RMSwitch;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.common.DateTimePickerHelper;
import org.pl.android.drively.common.DialogHelper;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.ui.base.progress.BaseProgressActivity;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFinanceActivity extends BaseProgressActivity implements AddFinanceMvpView, DatePickerDialog.OnDateSetListener {

    @Inject
    AddFinancePresenter addFinancePresenter;

    @BindView(R.id.finance_type_switch)
    RMSwitch financeTypeSwitch;

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.finance_amount_input)
    EditText amountInput;

    @BindView(R.id.finance_description_input)
    EditText descriptionInput;

    @BindView(R.id.finance_note_input)
    EditText noteInput;

    @BindView(R.id.category_input)
    TextView categoryInput;

    private String selectedCategory;

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
        date.setText(DateTimePickerHelper.getStringFromDate(new Date()));
        addFinancePresenter.loadCategories();
    }

    @OnClick(R.id.add_finance_button)
    public void addFinanceButtonClick(View view) {
        if (financeTypeSwitch.isChecked()) {
            try {
                Income income = Income.builder()
                        .amount(amountInput.getText().toString())
                        .date(DateTimePickerHelper.getDateFromString(date.getText().toString()))
                        .category(selectedCategory)
                        .description(descriptionInput.getText().toString())
                        .note(noteInput.getText().toString())
                        .build();
                addFinancePresenter.saveIncome(income);
            } catch (ParseException e) {
                hideProgressDialog();
                showMessage(R.string.finance_failed_to_add);
            }
        } else {
            try {
                Expense expense = Expense.builder()
                        .amount(amountInput.getText().toString())
                        .date(DateTimePickerHelper.getDateFromString(date.getText().toString()))
                        .category(selectedCategory)
                        .description(descriptionInput.getText().toString())
                        .note(noteInput.getText().toString())
                        .build();
                addFinancePresenter.saveExpense(expense);
            } catch (ParseException e) {
                hideProgressDialog();
                showMessage(R.string.finance_failed_to_add);
            }
        }
        showProgressDialog(R.string.finance_adding);
    }

    @OnClick(R.id.date_icon)
    public void dateIconClick(View view) {
        DateTimePickerHelper.showDatePicker(this, getFragmentManager(), ContextCompat.getColor(this, R.color.button_background));
    }

    @OnClick(R.id.category_input)
    public void categoryInputClick(View view) {
        showCheckboxDialog();
    }

    @OnClick(R.id.category_label)
    public void categoryLabelClick(View view) {
        showCheckboxDialog();
    }

    private void showCheckboxDialog() {
        DialogHelper.showCheckboxListDialog(this, addFinancePresenter.getCategories(),
                selectedCategory != null ? addFinancePresenter.getCategories().indexOf(selectedCategory) : -1,
                selectedCategory -> {
                    categoryInput.setText(selectedCategory);
                    this.selectedCategory = selectedCategory;
                });
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        date.setText(DateTimePickerHelper.getStringFromDate(cal.getTime()));
    }

}
