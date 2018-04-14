package org.pl.android.drively.ui.finance.form.add;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.common.DateTimePickerHelper;
import org.pl.android.drively.ui.finance.form.BaseFinanceFormActivity;
import org.pl.android.drively.util.GalleryUtil;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFinanceActivity extends BaseFinanceFormActivity implements AddFinanceMvpView, DatePickerDialog.OnDateSetListener {

    @Inject
    AddFinancePresenter addFinancePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_finance);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        addFinancePresenter.attachView(this);
        setBaseFinanceFormPresenter(addFinancePresenter);
        initializeView();
    }

    @OnClick(R.id.add_finance_button)
    public void addFinanceButtonClick(View view) {
        if (isFormValid()) {
            showProgressDialog(R.string.finance_adding);
            determineWhatAndAdd();
        }
    }

    private void determineWhatAndAdd() {
        if (financeTypeSwitch.isChecked()) {
            addIncome();
        } else {
            addExpense();
        }
    }

    private boolean isFormValid() {
        boolean isValid = true;
        if (amountInput.getText().toString().substring(0, 1).equals("0")) {
            amountInput.setError(getString(R.string.wrong_amount));
            isValid = false;
        }
        return isValid;
    }

    @OnClick(R.id.category_input)
    public void categoryInputClick(View view) {
        showCheckboxDialog();
    }

    @OnClick(R.id.category_label)
    public void categoryLabelClick(View view) {
        showCheckboxDialog();
    }

    @OnClick(R.id.attachment_button)
    public void startPickingPhoto(View view) {
        GalleryUtil.startPickingPhoto(this, PICK_IMAGE_REQUEST);
    }

    @OnClick(R.id.date_icon)
    public void dateIconClick(View view) {
        DateTimePickerHelper.showDatePicker(this, getFragmentManager(), ContextCompat.getColor(this, R.color.button_background));
    }

}
