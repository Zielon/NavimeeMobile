package org.pl.android.drively.ui.base.progress;

import android.os.Bundle;
import android.widget.Toast;

import org.pl.android.drively.ui.base.BaseActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;
import java8.util.Optional;

public class BaseProgressActivity extends BaseActivity implements BaseProgressMvp {

    private SweetAlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgressDialog();
    }

    private void initializeProgressDialog() {
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
    }

    @Override
    public void showProgressDialog(String content) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            progressDialog.setTitleText(content);
            progressDialog.show();
        });
    }

    @Override
    public void showProgressDialog(int stringResId) {
        Optional.ofNullable(progressDialog).ifPresent(progressDialog -> {
            progressDialog.setTitleText(getString(stringResId));
            progressDialog.show();
        });
    }

    @Override
    public void hideProgressDialog() {
        Optional.ofNullable(progressDialog).ifPresent(SweetAlertDialog::dismiss);
    }

    @Override
    public void showMessage(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int stringResId) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_SHORT).show();
    }

}
