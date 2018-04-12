package org.pl.android.drively.ui.finance.form.edit;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.common.DateTimePickerHelper;
import org.pl.android.drively.common.DialogHelper;
import org.pl.android.drively.data.model.Expense;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.form.BaseFinanceFormActivity;
import org.pl.android.drively.util.AnimationUtil;
import org.pl.android.drively.util.GalleryUtil;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

public class EditFinanceActivity extends BaseFinanceFormActivity implements EditFinanceMvpView, DatePickerDialog.OnDateSetListener {

    public static final String FINANCE_INTENT = "FINANCE_INTENT";
    public static final String FINANCE_TYPE = "FINANCE_TYPE";

    @Inject
    EditFinancePresenter editFinancePresenter;

    Finance finance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_finance);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        editFinancePresenter.attachView(this);
        setBaseFinanceFormPresenter(editFinancePresenter);
        initializeView();
        toggleFormAllowance(false);
        getFinanceFromIntentAndSetData();
    }

    private void getFinanceFromIntentAndSetData() {
        finance = new Gson().fromJson(getIntent().getStringExtra(FINANCE_INTENT), Finance.class);
        categoryInput.setText(finance.getCategory());
        selectedCategory = finance.getCategory();
        amountInput.setText(finance.getAmount());
        descriptionInput.setText(finance.getNote());
        noteInput.setText(finance.getDescription());
        date.setText(DateTimePickerHelper.getStringFromDate(finance.getDate()));
        editingId = finance.getId();
        if (finance.getAttachmentPath() != null) {
            financePhotoLoading.setVisibility(View.VISIBLE);
            setBitmap(finance);
        }
    }

    @OnClick(R.id.edit_finance_button)
    public void editFinanceButtonClick(View view) {
        if (((FancyButton) view).getText().equals(getString(R.string.add_finance))) {
            showProgressDialog(R.string.finance_adding);
            addExpense();
        } else {
            AnimationUtil.animateFadeOutWithEndCallback(view, () -> {
                ((FancyButton) view).setText(getString(R.string.add_finance));
                AnimationUtil.animateFlipIn(view);
            });
            toggleFormAllowance(true);
        }
    }

    private void toggleFormAllowance(boolean allow) {
        List<EditText> editTextList = Arrays.asList(amountInput, descriptionInput, noteInput);
        Stream.of(editTextList).forEach(editText -> editText.setEnabled(allow));
        if (allow) {
            categoryInput.setOnClickListener(view -> showCheckboxDialog());
            categoryLabel.setOnClickListener(view -> showCheckboxDialog());
            dateIcon.setOnClickListener(view -> DateTimePickerHelper.showDatePicker(this, getFragmentManager(), ContextCompat.getColor(this, R.color.button_background)));
            attachmentButton.setOnClickListener(view -> GalleryUtil.startPickingPhoto(this, PICK_IMAGE_REQUEST));
            attachmentButton.setVisibility(View.VISIBLE);
        } else {
            attachmentButton.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.back_finance_button)
    public void backFinanceButtonClick(View view) {
        finishActivity();
    }

    public void setBitmap(Finance finance) {
        financePhoto.setVisibility(View.VISIBLE);
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(editFinancePresenter.getFinanceImageReference(finance))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .signature(new MediaStoreSignature("", System.currentTimeMillis(), 0))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        financeBitmap = resource;
                        financePhotoLoading.setVisibility(View.GONE);
                        financePhoto.setImageBitmap(resource);
                    }
                });
    }

    @OnClick(R.id.delete_finance_button)
    public void deleteFinanceButtonClick(View view) {
        DialogHelper.showConfirmationDialog(this, getString(R.string.delete_finance_confirmation),
                () -> {
                    showProgressDialog(R.string.deleting);
                    if (getIntent().getStringExtra(FINANCE_TYPE).equals(Expense.class.getName())) {
                        editFinancePresenter.deleteExpense(finance.getId());
                    } else {
                        editFinancePresenter.deleteIncome(finance.getId());
                    }
                });
    }

    @Override
    public void goBackToFinances() {
        onBackPressed();
    }
}
