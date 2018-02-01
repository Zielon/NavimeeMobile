package org.pl.android.drively.ui.signinup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.regulations.RegulationsActivity;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Wojtek on 2018-02-01.
 */

public abstract class BaseSignActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "BaseSignActivity";
    private static final int REQUEST_SIGNUP = 0;
    @BindView(R.id.facebook_login_button)
    LoginButton facebookButton;
    @BindView(R.id.sign_in_google_button)
    SignInButton googleButton;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    protected ProgressDialog progressDialog;

    protected abstract  void onErrorFacebook();
    protected abstract void loginInWithFacebookOrGoogle(AuthCredential credential);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    public void initializeSocialButtons() {
        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        // FACEBOOK BUTTON
        mCallbackManager = CallbackManager.Factory.create();
        facebookButton.setReadPermissions("email", "public_profile");
        facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onCancel() {
                Timber.d(TAG, "facebook:onCancel");
                onErrorFacebook();
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onError(FacebookException error) {
                Timber.d(TAG, "facebook:onError", error);
                onErrorFacebook();
            }
        });

        setGooglePlusButtonText(googleButton, "Google");
    }

    //AUTH WITH FACEBOOK
    @SuppressLint("TimberArgCount")
    private void handleFacebookAccessToken(AccessToken token) {
        Timber.d(TAG, "handleFacebookAccessToken:" + token);
        progressDialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        loginInWithFacebookOrGoogle(credential);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        loginInWithFacebookOrGoogle(credential);
    }
    // [END auth_with_google]

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            buttonText += "           "; // A workaround for centering text in the button.
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
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

    @OnClick(R.id.regulation)
    public void showRegulation() {
        Intent intent = new Intent(this, RegulationsActivity.class);
        this.startActivityForResult(intent, 0);
    }

}
