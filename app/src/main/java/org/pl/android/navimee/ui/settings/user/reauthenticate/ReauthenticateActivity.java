package org.pl.android.navimee.ui.settings.user.reauthenticate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;
import org.pl.android.navimee.ui.settings.user.email.UserEmailChangeActivity;

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

    ProgressDialog _progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_reauthenticate);
        ButterKnife.bind(this);
        activityComponent().inject(this);

        _reauthenticatePresenter.attachView(this);

        _reauthenticateButton.setOnClickListener(v -> {

            _progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            _progressDialog.setIndeterminate(true);
            _progressDialog.setMessage(getResources().getString(R.string.login_progress));

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

            if (!valid) return;

            _progressDialog.show();
            _reauthenticateButton.setEnabled(false);
            _reauthenticatePresenter.reauthenticate(email, password);
        });
    }

    @Override
    public void onSuccess() {
        _reauthenticatePresenter.detachView();
        _reauthenticateButton.setEnabled(true);
        _progressDialog.dismiss();

        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        _passwordText.setText("");
        _emailText.setText("");
        _progressDialog.dismiss();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
