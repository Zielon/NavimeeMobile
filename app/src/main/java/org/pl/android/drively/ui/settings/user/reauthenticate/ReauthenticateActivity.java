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
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.drively.util.UserInputValidation.isEmailValid;
import static org.pl.android.drively.util.UserInputValidation.isPasswordValid;

public class ReauthenticateActivity extends BaseActivity implements ReauthenticateMvpView {

    @BindView(R.id.input_email)
    EditText editText;
    @BindView(R.id.input_password)
    TextInputEditText passwordText;
    @BindView(R.id.reauthenticate)
    Button reauthenticateButton;
    @Inject
    ReauthenticatePresenter reauthenticatePresenter;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_reauthenticate);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        HideKeyboard.setupUI(findViewById(R.id.reauthenticate_layout), this);

        reauthenticatePresenter.attachView(this);

        reauthenticateButton.setOnClickListener(v -> {

            progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getResources().getString(R.string.login_progress));

            String email = editText.getText().toString();
            String password = passwordText.getText().toString();

            boolean valid = true;

            if (!isEmailValid(email)) {
                editText.setError(getResources().getString(R.string.valid_email_address));
                valid = false;
            } else {
                editText.setError(null);
            }

            if (!isPasswordValid(password)) {
                passwordText.setError(getResources().getString(R.string.valid_password));
                valid = false;
            } else {
                passwordText.setError(null);
            }

            if (!valid) return;

            progressDialog.show();
            reauthenticateButton.setEnabled(false);
            reauthenticatePresenter.reauthenticate(email, password);
        });
    }

    @Override
    public void onSuccess() {
        reauthenticatePresenter.detachView();
        reauthenticateButton.setEnabled(true);
        progressDialog.dismiss();

        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        passwordText.setText("");
        editText.setText("");
        progressDialog.dismiss();
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
