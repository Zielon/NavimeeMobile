package org.pl.android.drively.ui.signinup.up;


import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.signinup.BaseSignActivity;
import org.pl.android.drively.ui.signinup.BaseSignMvpView;
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.pl.android.drively.util.UserInputValidation.isEmailValid;
import static org.pl.android.drively.util.UserInputValidation.isPasswordValid;

public class SignUpActivity extends BaseSignActivity implements BaseSignMvpView {

    private static final String TAG = "SignUpActivity";

    @Inject
    SignUpPresenter signUpPresenter;

    @BindView(R.id.input_name)
    EditText _nameText;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

    @BindView(R.id.title)
    TextView titleTextView;

    @BindView(R.id.btn_signup)
    Button signupButton;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);
        activityComponent().inject(SignUpActivity.this);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        signUpPresenter.attachView(this);


        progressDialog.setMessage(getResources().getString(R.string.create_account_progress));

        HideKeyboard.setupUI(findViewById(R.id.sing_up_layout), this);

        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));
        signupButton.setOnClickListener(v -> signUp());
        initializeSocialButtons();
    }

    @Override
    protected void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @Override
    protected void showDialog() {
        progressDialog.show();
    }

    @Override
    protected void loginInWithFacebookOrGoogle(AuthCredential credential) {
        signUpPresenter.loginInWithFacebookOrGoogle(credential);
    }

    @SuppressLint("TimberArgCount")
    public void signUp() {
        Timber.d(TAG, "Signup");


        progressDialog.show();

        if (!validate()) {
            onError(null);
            return;
        }

        signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        signUpPresenter.register(email, password, name);
    }


    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty()) {
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
        signupButton.setEnabled(true);
        signUpPresenter.registerMessagingToken();
        signUpPresenter.saveUserInfo();
        progressDialog.dismiss();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError(Throwable throwable) {
        progressDialog.dismiss();

        if (throwable != null && throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
        progressDialog.dismiss();
    }

}