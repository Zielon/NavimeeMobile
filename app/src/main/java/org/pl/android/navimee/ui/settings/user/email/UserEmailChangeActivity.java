package org.pl.android.navimee.ui.settings.user.email;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.navimee.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.navimee.util.UserInputValidation.isEmailValid;

public class UserEmailChangeActivity extends BaseActivity implements UserEmailChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.save)
    Button _saveButton;
    @Inject
    UserEmailChangePresenter _userEmailChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_email_change);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        HideKeyboard.setupUI(findViewById(R.id.layout_email_change), this);

        _userEmailChangePresenter.attachView(this);

        _saveButton.setOnClickListener(v -> {
            if (!isEmailValid(_emailText.getText().toString())) {
                _emailText.setError(getResources().getString(R.string.valid_email_address));
                return;
            } else
                _emailText.setError(null);

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String newEmail = _emailText.getText().toString();
                if (isEmailValid(newEmail))
                    _userEmailChangePresenter.changeEmail(newEmail);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_succeed), Toast.LENGTH_LONG).show();
        _userEmailChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null && throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_failed), Toast.LENGTH_LONG).show();

        _emailText.setText("");
    }
}
