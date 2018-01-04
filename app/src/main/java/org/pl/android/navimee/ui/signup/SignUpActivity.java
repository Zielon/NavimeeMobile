package org.pl.android.navimee.ui.signup;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SignUpActivity extends BaseActivity implements SignUpMvpView {

    private static final String TAG = "SignUpActivity";

    @Inject
    SignUpPresenter mSignUpPresenter;

    @BindView(R.id.input_name)
    EditText _nameText;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    EditText _passwordText;

    @BindView(R.id.title)
    TextView _titleTextView;

    @BindView(R.id.btn_signup)
    Button _signupButton;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mSignUpPresenter.attachView(this);

        _titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));

        _signupButton.setOnClickListener(v -> signUp());
    }

    public void signUp() {
        Timber.d(TAG, "Signup");

        progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.create_account_progress));
        progressDialog.show();

        if (!validate()) {
            onError();
            return;
        }

        _signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        mSignUpPresenter.register(email, password, name);
    }


    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 4) {
            _nameText.setError(getResources().getString(R.string.valid_name));
            valid = false;
        } else {
            _nameText.setError(null);
        }

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

        return valid;
    }

    @Override
    public void onSuccess() {
        _signupButton.setEnabled(true);
        mSignUpPresenter.registerMessagingToken();
        progressDialog.dismiss();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        progressDialog.dismiss();
    }
}