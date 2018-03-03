package org.pl.android.drively.ui.settings.user.email;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pl.android.drively.util.UserInputValidation.isEmailValid;

public class UserEmailChangeActivity extends BaseActivity implements UserEmailChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.input_email)
    EditText editText;
    @BindView(R.id.save)
    Button saveButton;
    @Inject
    UserEmailChangePresenter userEmailChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_email_change);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        HideKeyboard.setupUI(findViewById(R.id.layout_email_change), this);

        userEmailChangePresenter.attachView(this);

        editText.setText(userEmailChangePresenter.getEmail());

        saveButton.setOnClickListener(v -> {
            if (!isEmailValid(editText.getText().toString())) {
                editText.setError(getResources().getString(R.string.valid_email_address));
                return;
            } else
                editText.setError(null);

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String newEmail = editText.getText().toString();
                if (isEmailValid(newEmail))
                    userEmailChangePresenter.changeEmail(newEmail);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_succeed), Toast.LENGTH_LONG).show();
        userEmailChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null && throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_failed), Toast.LENGTH_LONG).show();

        editText.setText("");
    }
}
