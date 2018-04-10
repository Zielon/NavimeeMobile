package org.pl.android.drively.ui.base.progress;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;
import java8.util.Optional;
import timber.log.Timber;

public class BaseProgressFragment extends Fragment implements BaseProgressMvp {

    private SweetAlertDialog progressDialog;

    protected void initializeProgressDialog() {
        progressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
    }

    @Override
    public void showProgressDialog(String content) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            Timber.d("okukubambo, showing progress");
            progressDialog.setTitleText(content);
            progressDialog.show();
        });
    }

    @Override
    public void showProgressDialog(int stringResId) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            Timber.d("okukubambo, showing progress");
            progressDialog.setTitleText(getString(stringResId));
            progressDialog.show();
        });
    }

    @Override
    public void hideProgressDialog() {
        Optional.ofNullable(progressDialog).ifPresent(sweetAlertDialog ->
                getActivity().runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Timber.d("okukubambo, hiding progress");
                }));
    }

    @Override
    public void showMessage(String content) {
        try {
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            Log.d("Showing message failed.", e.getMessage());
        }
    }

    @Override
    public void showMessage(int stringResId) {
        try {
            Toast.makeText(getActivity(), getString(stringResId), Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            Log.d("Showing message failed.", e.getMessage());
        }
    }

}
