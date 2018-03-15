package org.pl.android.drively.ui.signinup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.main.MainActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        if (throwable != null && throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

}