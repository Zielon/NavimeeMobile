package org.pl.android.drively.ui.signinup.in;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;

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

public class SignInActivity extends BaseSignActivity implements BaseSignMvpView {

    @Inject
    SignInPresenter mSignInPresenter;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText passwordText;

    @BindView(R.id.btn_login)
    Button loginButton;

    @BindView(R.id.title)
    TextView titleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);
        activityComponent().inject(this);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));
        HideKeyboard.setupUI(findViewById(R.id.sing_in_layout), this);
        mSignInPresenter.attachView(this);
        progressDialog.setMessage(getResources().getString(R.string.login_progress));
        loginButton.setOnClickListener(v -> login());
        initializeSocialButtons();
    }

    public void login() {
        Timber.d("Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }
        loginButton.setEnabled(false);
        progressDialog.show();
        String email = _emailText.getText().toString();
        String password = passwordText.getText().toString();
        mSignInPresenter.loginIn(email, password);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();
        String password = passwordText.getText().toString();
        if (!isEmailValid(email)) {
            _emailText.setError(getResources().getString(R.string.valid_email_address));
            valid = false;
        } else {
            _emailText.setError(null);
        }
        if (!isPasswordValid(password)) {
            passwordText.setError(getResources().getString(R.string.valid_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }
        return valid;
    }

    @Override
    public void onSuccess() {
        loginButton.setEnabled(true);
        mSignInPresenter.registerMessagingToken();
        mSignInPresenter.saveUserInfo();
        progressDialog.dismiss();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSignInPresenter.detachView();
    }

    @Override
    public void onError(Throwable throwable) {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        loginButton.setEnabled(true);
    }

    protected void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @Override
    protected void loginInWithFacebookOrGoogle(AuthCredential credential) {
        mSignInPresenter.loginInWithFacebookOrGoogle(credential);
    }

}

