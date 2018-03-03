package org.pl.android.drively.ui.settings.user.password;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.Toast;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.drively.util.UserInputValidation.isPasswordValid;

public class UserPasswordChangeActivity extends BaseActivity implements UserPasswordChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.save)
    Button saveButton;
    @BindView(R.id.input_password)
    TextInputEditText passwordText;
    @Inject
    UserPasswordChangePresenter userPasswordChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_change);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        HideKeyboard.setupUI(findViewById(R.id.layout_password_change), this);

        userPasswordChangePresenter.attachView(this);

        saveButton.setOnClickListener(v -> {
            if (!isPasswordValid(passwordText.getText().toString())) {
                passwordText.setError(getResources().getString(R.string.valid_password));
                return;
            } else
                passwordText.setError(null);

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String password = passwordText.getText().toString();
                if (isPasswordValid(password))
                    userPasswordChangePresenter.changePassword(password);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_succeed), Toast.LENGTH_LONG).show();
        userPasswordChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_failed), Toast.LENGTH_LONG).show();
        passwordText.setText("");
    }
}
