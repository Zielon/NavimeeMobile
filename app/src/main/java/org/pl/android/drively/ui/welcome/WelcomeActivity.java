package org.pl.android.drively.ui.welcome;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.ui.signin.SignInActivity;
import org.pl.android.drively.ui.signup.SignUpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends BaseActivity {

    private static final int REQUEST_SIGN = 1;

    @BindView(R.id.loginButton)
    Button _loginButton;

    @BindView(R.id.createAccountButton)
    Button _createAccountButton;

    @BindView(R.id.title)
    TextView _titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        _titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));

        _loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivityForResult(intent, REQUEST_SIGN);
        });

        _createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGN);
        });
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
}