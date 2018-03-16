package org.pl.android.drively.ui.signinup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.main.MainActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignActivity extends BaseSignActivity implements BaseSignMvpView {

    @Inject
    SignPresenter mSignPresenter;

    @BindView(R.id.title)
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_sign);
        ButterKnife.bind(this);
        mSignPresenter.attachView(this);
        progressDialog.setMessage(getResources().getString(R.string.login_progress));
        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));
        initializeSocialButtons();
    }


    @Override
    protected void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @Override
    protected void loginInWithFacebookOrGoogle(AuthCredential credential) {
        mSignPresenter.loginInWithFacebookOrGoogle(credential);
    }

    @Override
    public void onSuccess() {
        mSignPresenter.registerUser().addOnCompleteListener(user -> {
            progressDialog.dismiss();
            setResult(RESULT_OK, null);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        });
    }

    @Override
    public void onError(Throwable throwable) {
        progressDialog.dismiss();
        Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @OnClick(R.id.replaced_sing_in_google_button)
    public void replacedSignInGoogleButtonClick(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.replaced_facebook_login_button)
    public void replacedFacebookLoginButtonClick(View view) {
        facebookButton.performClick();
    }
}