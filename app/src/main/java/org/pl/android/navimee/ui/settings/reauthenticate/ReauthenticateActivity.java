package org.pl.android.navimee.ui.settings.reauthenticate;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReauthenticateActivity extends BaseActivity implements UserSettingsChangeMvpView {

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    EditText _passwordText;

    @BindView(R.id.reauthenticate)
    Button _reauthenticateButton;

    @Inject
    ReauthenticatePresenter _reauthenticatePresenter;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reauthenticate);
        ButterKnife.bind(this);
        activityComponent().inject(this);

        _reauthenticatePresenter.attachView(this);

        _reauthenticateButton.setOnClickListener(v -> {

            progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getResources().getString(R.string.create_account_progress));
            progressDialog.show();

            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            boolean valid = true;

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _emailText.setError(getResources().getString(R.string.valid_email_address));
                valid = false;
            } else {
                _emailText.setError(null);
            }

            if (password.isEmpty() || password.length() < 6 || password.length() > 10) {
                _passwordText.setError(getResources().getString(R.string.valid_password));
                valid = false;
            } else {
                _passwordText.setError(null);
            }

            if(!valid) return;
            _reauthenticateButton.setEnabled(false);
            _reauthenticatePresenter.reauthenticate(email, password);
        });
    }

    @Override
    public void onSuccess() {
        _reauthenticatePresenter.detachView();
        _reauthenticateButton.setEnabled(true);

        progressDialog.dismiss();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        _passwordText.setText("");
        _emailText.setText("");
    }
}
