package org.pl.android.drively.ui.settings.user.reauthenticate;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.settings.user.UserSettingsChangeMvpView;
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.drively.util.UserInputValidation.isEmailValid;
import static org.pl.android.drively.util.UserInputValidation.isPasswordValid;

public class ReauthenticateActivity extends BaseActivity implements UserSettingsChangeMvpView {

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

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
        HideKeyboard.setupUI(findViewById(R.id.reauthenticate_layout), this);

        _reauthenticatePresenter.attachView(this);

        _reauthenticateButton.setOnClickListener(v -> {

            _progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            _progressDialog.setIndeterminate(true);
            _progressDialog.setMessage(getResources().getString(R.string.login_progress));

            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            boolean valid = true;

            if (!isEmailValid(email)) {
                _emailText.setError(getResources().getString(R.string.valid_email_address));
                valid = false;
            } else {
                _emailText.setError(null);
            }

            if (!isPasswordValid(password)) {
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
