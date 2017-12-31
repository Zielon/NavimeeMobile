package org.pl.android.navimee.ui.settings.user;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserEmailChangeActivity extends BaseActivity {

    @BindView(R.id.input_email)
    EditText _emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_email_change);
        ButterKnife.bind(this);
    }

    public void onClick(View v){
        String email = _emailText.getText().toString();
        boolean isValid = true;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getResources().getString(R.string.valid_email_address));
            isValid = false;
        } else {
            _emailText.setError(null);
        }

        if(!isValid) return;
    }
}
