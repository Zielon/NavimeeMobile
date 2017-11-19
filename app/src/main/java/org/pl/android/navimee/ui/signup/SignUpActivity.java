package org.pl.android.navimee.ui.signup;

/**
 * Created by Wojtek on 2017-10-24.
 */

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
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

    @Inject
    SignUpPresenter mSignUpPresenter;

    private static final String TAG = "SignUpActivity";

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mSignUpPresenter.attachView(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signUp() {
        Timber.d(TAG, "Signup");

        if (!validate()) {
            onError();
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.create_account_progress));
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.
        mSignUpPresenter.register(email,password);

    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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
        setResult(RESULT_OK, null);
        finish();
        progressDialog.dismiss();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(),getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);

        progressDialog.dismiss();
    }
}