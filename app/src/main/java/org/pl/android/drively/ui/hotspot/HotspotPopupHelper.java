package org.pl.android.drively.ui.hotspot;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.util.Const;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.facebook.FacebookSdk.getApplicationContext;

public class HotspotPopupHelper {

    private static MaterialDialog firstPopup;

    private static MaterialDialog secondPopup;

    private static Const.DriverType selectedDriverType;

    public static void showFirstPopup(Context context, String driverType, OnSuccessCallback onSuccessCallback, OnFailureCallback onFailureCallback) {
        if (firstPopup != null) {
            firstPopup.dismiss();
        }
        selectedDriverType = null;
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.hotspot_popup_instruction, null);
        firstPopup = new MaterialDialog.Builder(context)
                .customView(rootView, false)
                .backgroundColor(ContextCompat.getColor(context, R.color.dark_transparent))
                .canceledOnTouchOutside(false)
                .show();
        for (Const.DriverType driverType2 : Const.DriverType.values()) {
            rootView.findViewById(driverType2.getButtonResId()).setOnClickListener(view -> {
                for (Const.DriverType driverTypeDeselect : Const.DriverType.values()) {
                    rootView.findViewById(driverTypeDeselect.getButtonResId()).setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    ((FancyButton) rootView.findViewById(driverTypeDeselect.getButtonResId())).setTextColor(ContextCompat.getColor(context, R.color.filters_buttons));
                }
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.filters_buttons));
                ((FancyButton) view).setTextColor(ContextCompat.getColor(context, R.color.white));
                selectedDriverType = driverType2;
                rootView.findViewById(R.id.brand_error).setVisibility(View.GONE);
            });
        }
        rootView.findViewById(R.id.agree_button)
                .setOnClickListener(view -> {
                    if (selectedDriverType == null) {
                        rootView.findViewById(R.id.brand_error).setVisibility(View.VISIBLE);
                    } else {
                        firstPopup.dismiss();
                        onSuccessCallback.onSuccessCallback(selectedDriverType);
                    }
                });
        rootView.findViewById(R.id.dismiss_dialog).setOnClickListener(view -> {
            onFailureCallback.onFailureCallback();
            firstPopup.dismiss();
        });
        if (driverType != null) {
            changeFirstPopupContent(context, driverType, rootView);
        }
    }

    private static void changeFirstPopupContent(Context context, String driverType, View rootView) {
        if (!TextUtils.isEmpty(driverType)) {
            selectedDriverType = Const.DriverType.getByName(driverType);
            rootView.findViewById(Const.DriverType.getByName(driverType).getButtonResId())
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.filters_buttons));
            ((FancyButton) rootView.findViewById(Const.DriverType.getByName(driverType).getButtonResId()))
                    .setTextColor(ContextCompat.getColor(context, R.color.white));
            ((TextView) rootView.findViewById(R.id.popup_hotspot_agreement)).setText(R.string.popup_hotspot_agreement_change);
            ((TextView) rootView.findViewById(R.id.popup_hotspot_brand_question)).setText(R.string.popup_hotspot_brand_question_change);
            ((FancyButton) rootView.findViewById(R.id.agree_button)).setText(context.getString(R.string.popup_hotspot_button_agreement_change));
        }
    }

    public static void showSecondPopup(Context context) {
        if (secondPopup != null) {
            secondPopup.dismiss();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.hotspot_popup_instruction_second, null);
        secondPopup = new MaterialDialog.Builder(context)
                .customView(view, false)
                .backgroundColor(ContextCompat.getColor(context, R.color.transparent))
                .show();
        view.findViewById(R.id.popup_hotspot_second_agree_button).setOnClickListener(s -> secondPopup.dismiss());
    }

    public interface OnSuccessCallback {
        void onSuccessCallback(Const.DriverType driverType);
    }

    public interface OnFailureCallback {
        void onFailureCallback();
    }

}
