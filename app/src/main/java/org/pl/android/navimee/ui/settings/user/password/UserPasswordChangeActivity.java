package org.pl.android.navimee.ui.settings.user.password;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.Toast;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;
import org.pl.android.navimee.ui.settings.user.reauthenticate.ReauthenticateActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.navimee.util.UserInputValidation.isPasswordValid;

public class UserPasswordChangeActivity extends BaseActivity implements UserSettingsChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.save)
    Button _saveButton;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

    @Inject
    UserPasswordChangePresenter _userPasswordChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_change);
        ButterKnife.bind(this);
        activityComponent().inject(this);

        _userPasswordChangePresenter.attachView(this);

        _saveButton.setOnClickListener(v -> {
            if (!isPasswordValid(_passwordText.getText().toString())) {
                _passwordText.setError(getResources().getString(R.string.valid_password));
                return;
            } else
                _passwordText.setError(null);

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String password = _passwordText.getText().toString();
                if (isPasswordValid(password))
                    _userPasswordChangePresenter.changePassword(password);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_succeed), Toast.LENGTH_LONG).show();
        _userPasswordChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_failed), Toast.LENGTH_LONG).show();
        _passwordText.setText("");
    }
}
