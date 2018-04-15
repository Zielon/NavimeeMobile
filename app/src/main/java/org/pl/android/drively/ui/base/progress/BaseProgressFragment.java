package org.pl.android.drively.ui.base.progress;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.pl.android.drively.R;

import java8.util.Optional;

public class BaseProgressFragment extends Fragment implements BaseProgressMvp {

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgressDialog();
    }

    protected void initializeProgressDialog() {
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    @Override
    public void showProgressDialog(String content) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            progressDialog.setMessage(content);
            progressDialog.show();
        });
    }

    @Override
    public void showProgressDialog(int stringResId) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            progressDialog.setMessage(getString(stringResId));
            progressDialog.show();
        });
    }

    @Override
    public void hideProgressDialog() {
        Optional.ofNullable(progressDialog).ifPresent(sweetAlertDialog ->
                Optional.ofNullable(getActivity()).ifPresent(activity -> activity.runOnUiThread(sweetAlertDialog::dismiss)));
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
