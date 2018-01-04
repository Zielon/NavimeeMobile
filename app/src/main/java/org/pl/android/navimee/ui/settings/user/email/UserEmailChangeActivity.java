package org.pl.android.navimee.ui.settings.user.email;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.reauthenticate.ReauthenticateActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserEmailChangeActivity extends BaseActivity implements UserSettingsChangeMvpView {

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.save)
    Button _saveButton;

    @Inject
    UserEmailChangePresenter _userEmailChangePresenter;

    private static final int REQUEST_REAUTHENTICATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_email_change);
        ButterKnife.bind(this);
        activityComponent().inject(this);

        _userEmailChangePresenter.attachView(this);

        _saveButton.setOnClickListener(v -> {
            String email = _emailText.getText().toString();
            boolean isValid = isEmailValid(email);
            if(!isValid){
                _emailText.setError(getResources().getString(R.string.valid_email_address));
                return;
            }
            else
                _emailText.setError(null);

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_REAUTHENTICATE)
            if(resultCode == RESULT_OK){
                String newEmail = _emailText.getText().toString();
                if(isEmailValid(newEmail))
                    _userEmailChangePresenter.changeEmail(newEmail);
            }
    }

    private boolean isEmailValid(String email){
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onSuccess() {
        _userEmailChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
    }
}
