package org.pl.android.navimee.ui.welcome;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.signin.SignInActivity;
import org.pl.android.navimee.ui.signup.SignUpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends BaseActivity {

    @BindView(R.id.loginButton)
    Button loginButton;

    @BindView(R.id.createAccountButton)
    Button createAccountButton;

    @BindView(R.id.title)
    TextView titleTextView;

    private static final int REQUEST_SIGNUP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(),"NexaBold.ttf"));

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
        });

        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
        });
    }
}
