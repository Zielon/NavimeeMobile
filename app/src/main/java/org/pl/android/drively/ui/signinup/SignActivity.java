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

    private static final int REQUEST_SIGN = 1;

    @Inject
    SignPresenter mSignPresenter;

    @BindView(R.id.title)
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        initializeSocialButtons();

        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGN) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
            }
        }
    }

    @Override
    public void onSuccess() {
        mSignPresenter.registerUser().addOnCompleteListener(user -> {
            progressDialog.dismiss();
            setResult(RESULT_OK, null);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSignPresenter.detachView();
    }

    @Override
    public void onError(Throwable throwable) {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    protected void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @Override
    protected void loginInWithFacebookOrGoogle(AuthCredential credential) {
        mSignPresenter.loginInWithFacebookOrGoogle(credential);
    }

    @OnClick(R.id.replaced_facebook_login_button)
    public void performFacebookLoginButtonClick(View view) {
        facebookButton.performClick();
    }

    @OnClick(R.id.replaced_sing_in_google_button)
    public void performGoogleLoginButtonClick(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}