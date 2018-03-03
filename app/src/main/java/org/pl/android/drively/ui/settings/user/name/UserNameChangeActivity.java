package org.pl.android.drively.ui.settings.user.name;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.settings.user.reauthenticate.ReauthenticateActivity;
import org.pl.android.drively.util.HideKeyboard;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserNameChangeActivity extends BaseActivity implements UserNameChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.save)
    Button saveButton;
    @BindView(R.id.input_name)
    EditText editText;
    @Inject
    UserNameChangePresenter userNameChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        ButterKnife.bind(this);
        activityComponent().inject(this);
        HideKeyboard.setupUI(findViewById(R.id.layout_name_change), this);

        userNameChangePresenter.attachView(this);

        editText.setText(userNameChangePresenter.getName());

        saveButton.setOnClickListener(v -> {
            String name = editText.getText().toString();

            if (name.equals("")) {
                editText.setError(getResources().getString(R.string.change_name_failed));
                return;
            }

            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String name = editText.getText().toString();
                userNameChangePresenter.changeName(name);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_name_succeed), Toast.LENGTH_LONG).show();
        userNameChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_name_failed), Toast.LENGTH_LONG).show();
        editText.setText("");
    }
}
