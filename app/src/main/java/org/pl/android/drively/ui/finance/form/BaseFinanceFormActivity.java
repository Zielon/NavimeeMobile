package org.pl.android.drively.ui.finance.form;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.rm.rmswitch.RMSwitch;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.common.DateTimePickerHelper;
import org.pl.android.drively.common.DialogHelper;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Income;
import org.pl.android.drively.ui.base.progress.BaseProgressActivity;
import org.pl.android.drively.util.GalleryUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Setter;
import mehdi.sakout.fancybuttons.FancyButton;

import static org.pl.android.drively.util.Const.FILE_MAX_SIZE_5_MB;

public abstract class BaseFinanceFormActivity extends BaseProgressActivity implements BaseFinanceFormMvp, DatePickerDialog.OnDateSetListener {

    protected static final int PICK_IMAGE_REQUEST = 8742;

    @Setter
    BaseFinanceFormPresenter baseFinanceFormPresenter;

    @BindView(R.id.finance_type_switch)
    protected RMSwitch financeTypeSwitch;

    @BindView(R.id.date)
    protected TextView date;

    @BindView(R.id.finance_amount_input)
    protected CurrencyEditText amountInput;

    @BindView(R.id.finance_description_input)
    protected EditText descriptionInput;

    @BindView(R.id.finance_note_input)
    protected EditText noteInput;

    @BindView(R.id.category_input)
    protected TextView categoryInput;

    @BindView(R.id.category_label)
    protected TextView categoryLabel;

    @BindView(R.id.finance_photo)
    protected CircleImageView financePhoto;

    @BindView(R.id.date_icon)
    protected ImageView dateIcon;

    @BindView(R.id.attachment_button)
    protected FancyButton attachmentButton;

    @BindView(R.id.finance_photo_loading)
    protected ProgressBar financePhotoLoading;

    protected String selectedCategory;

    protected Bitmap financeBitmap;

    protected String editingId;

    protected void initializeView() {
        date.setText(DateTimePickerHelper.getStringFromDate(new Date()));
        baseFinanceFormPresenter.loadCategories();
    }

    protected void addExpense() {
        try {
            Expense expense = Expense.builder()
                    .amount(amountInput.getText().toString())
                    .date(DateTimePickerHelper.getDateFromString(date.getText().toString()))
                    .category(selectedCategory)
                    .description(descriptionInput.getText().toString())
                    .note(noteInput.getText().toString())
                    .build();
            if (editingId != null) {
                expense.setId(editingId);
            }
            baseFinanceFormPresenter.saveExpenseWithBitmap(expense, financeBitmap);
        } catch (ParseException e) {
            hideProgressDialog();
            showMessage(R.string.finance_failed_to_add);
        }
    }

    protected void addIncome() {
        try {
            addIncome();
            Income income = Income.builder()
                    .amount(amountInput.getText().toString())
                    .date(DateTimePickerHelper.getDateFromString(date.getText().toString()))
                    .category(selectedCategory)
                    .description(descriptionInput.getText().toString())
                    .note(noteInput.getText().toString())
                    .build();
            if (editingId != null) {
                income.setId(editingId);
            }
            baseFinanceFormPresenter.saveIncomeWithBitmap(income, financeBitmap);
        } catch (ParseException e) {
            hideProgressDialog();
            showMessage(R.string.finance_failed_to_add);
        }
    }

    protected void showCheckboxDialog() {
        DialogHelper.showCheckboxListDialog(this, baseFinanceFormPresenter.getCategories(),
                selectedCategory != null ? baseFinanceFormPresenter.getCategories().indexOf(selectedCategory) : -1,
                selectedCategory -> {
                    categoryInput.setText(selectedCategory);
                    this.selectedCategory = selectedCategory;
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            if (GalleryUtil.checkSize(this, data.getData(), FILE_MAX_SIZE_5_MB)) {
                financeBitmap = null;
                try {
                    financeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (financeBitmap == null) {
                    showMessage(getString(R.string.incorrect_file));
                    return;
                }
                financePhoto.setImageBitmap(financeBitmap);
                financePhoto.setVisibility(View.VISIBLE);
            } else {
                showMessage(R.string.too_large_file);
            }
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        date.setText(DateTimePickerHelper.getStringFromDate(cal.getTime()));
    }

    @Override
    public void finishActivity() {
        onBackPressed();
    }

    @OnClick(R.id.finance_photo)
    public void onFinancePhotoClick(View view) {
        financePhoto.buildDrawingCache();
        Bitmap bitmap = financePhoto.getDrawingCache();
        GalleryUtil.zoomInImageView(this, view, new BitmapDrawable(getResources(), financeBitmap));
    }

}
