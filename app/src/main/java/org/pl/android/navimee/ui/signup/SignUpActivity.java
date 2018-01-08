package org.pl.android.navimee.ui.signup;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.pl.android.navimee.util.UserInputValidation.isEmailValid;
import static org.pl.android.navimee.util.UserInputValidation.isPasswordValid;

public class SignUpActivity extends BaseActivity implements SignUpMvpView {

    private static final String TAG = "SignUpActivity";

    @Inject
    SignUpPresenter _signUpPresenter;

    @BindView(R.id.input_name)
    EditText _nameText;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

    @BindView(R.id.title)
    TextView _titleTextView;

    @BindView(R.id.btn_signup)
    Button _signupButton;

    ProgressDialog _progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);
        activityComponent().inject(SignUpActivity.this);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        _signUpPresenter.attachView(this);

        _titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));

        _signupButton.setOnClickListener(v -> signUp());
    }

    public void signUp() {
        Timber.d(TAG, "Signup");

        _progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getResources().getString(R.string.create_account_progress));
        _progressDialog.show();

        if (!validate()) {
            onError(null);
            return;
        }

        _signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        _signUpPresenter.register(email, password, name);
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

        return valid;
    }

    @Override
    public void onSuccess() {
        _signupButton.setEnabled(true);
        _signUpPresenter.registerMessagingToken();
        _progressDialog.dismiss();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError(Throwable throwable) {
        _progressDialog.dismiss();

        if (throwable != null && throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
        _progressDialog.dismiss();
    }
}