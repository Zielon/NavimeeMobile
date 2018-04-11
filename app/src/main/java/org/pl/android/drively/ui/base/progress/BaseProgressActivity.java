package org.pl.android.drively.ui.base.progress;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;

import java8.util.Optional;

public class BaseProgressActivity extends BaseActivity implements BaseProgressMvp {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgressDialog();
    }

    protected void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
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
        Optional.ofNullable(progressDialog).ifPresent(ProgressDialog::dismiss);
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
