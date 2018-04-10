package org.pl.android.drively.common;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;

import java.util.List;

public class DialogHelper {

    public static void showCheckboxListDialog(Context context, List<String> input, int selectedIndex, CheckboxCallback checkboxCallback) {
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.category_label))
                .items(input)
                .itemsCallbackSingleChoice(selectedIndex, (dialog, itemView, which, text) -> {
                    checkboxCallback.checkboxCallback(input.get(which));
                    return false;
                })
                .show();
    }

    public static void showConfirmationDialog(Context context, String message, ConfirmCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.confirm_operation)
                .content(message)
                .positiveText(R.string.confirm_text)
                .negativeText(R.string.cancel_text)
                .onNegative((dialog, which) -> dialog.dismiss())
                .onPositive((dialog, which) -> callback.onConfirm())
                .show();
    }

    public interface CheckboxCallback {
        void checkboxCallback(String output);
    }

    public interface ConfirmCallback {
        void onConfirm();
    }

}
