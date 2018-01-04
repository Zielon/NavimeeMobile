package org.pl.android.navimee.ui.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SignInActivity extends BaseActivity implements SignInMvpView {

    @Inject
    SignInPresenter mSignInPresenter;

    @BindView(R.id.facebook_login_button)
    LoginButton facebookButton;

    @BindView(R.id.sign_in_google_button)
    SignInButton googleButton;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_password)
    EditText _passwordText;

    @BindView(R.id.btn_login)
    Button _loginButton;

    @BindView(R.id.title)
    TextView _titleTextView;

    ProgressDialog progressDialog;

    private static final int REQUEST_SIGNUP = 0;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        _titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));

        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();

        mSignInPresenter.attachView(this);

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.login_progress));

        initializeButtons();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void initializeButtons() {
        _loginButton.setOnClickListener(v -> login());

        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // FACEBOOK BUTTON
        mCallbackManager = CallbackManager.Factory.create();
        facebookButton.setReadPermissions("email", "public_profile");
        facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Timber.d(TAG, "facebook:onCancel");
                onErrorFacebook();
            }

            @Override
            public void onError(FacebookException error) {
                Timber.d(TAG, "facebook:onError", error);
                onErrorFacebook();
            }
        });

        setGooglePlusButtonText(googleButton, "Google");
    }


    public void login() {
        Timber.d("Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);


        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        mSignInPresenter.loginIn(email, password);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("Facebook");
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        } else if (requestCode == RC_SIGN_IN) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        } else {
            //facebook
            if (resultCode == RESULT_OK) {
                super.onActivityResult(requestCode, resultCode, data);
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }

        setResult(resultCode);
    }


    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
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

    //AUTH WITH FACEBOOK
    private void handleFacebookAccessToken(AccessToken token) {
        Timber.d(TAG, "handleFacebookAccessToken:" + token);
        progressDialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mSignInPresenter.loginInWithFacebookOrGoogle(credential);
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mSignInPresenter.loginInWithFacebookOrGoogle(credential);
    }
    // [END auth_with_google]

    @Override
    public void onSuccess() {
        _loginButton.setEnabled(true);
        mSignInPresenter.registerMessagingToken();
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
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        _loginButton.setEnabled(true);
    }

    public void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
}

