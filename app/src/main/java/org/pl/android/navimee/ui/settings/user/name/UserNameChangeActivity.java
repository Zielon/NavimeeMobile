package org.pl.android.navimee.ui.settings.user.name;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;
import org.pl.android.navimee.ui.settings.user.UserSettingsChangeMvpView;
import org.pl.android.navimee.ui.settings.user.reauthenticate.ReauthenticateActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserNameChangeActivity extends BaseActivity implements UserSettingsChangeMvpView {

    private static final int REQUEST_REAUTHENTICATE = 0;
    @BindView(R.id.save)
    Button _saveButton;
    @BindView(R.id.input_name)
    EditText _nameText;
    @Inject
    UserNameChangePresenter _userNameChangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        ButterKnife.bind(this);
        activityComponent().inject(this);

        _userNameChangePresenter.attachView(this);

        _saveButton.setOnClickListener(v ->{
            String name = _nameText.getText().toString();

            if(name.equals("")) {
                _nameText.setError(getResources().getString(R.string.change_name_failed));
                return;
            }
            
            this.startActivityForResult(new Intent(this, ReauthenticateActivity.class), REQUEST_REAUTHENTICATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REAUTHENTICATE)
            if (resultCode == RESULT_OK) {
                String name = _nameText.getText().toString();
                _userNameChangePresenter.changeName(name);
            }
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_name_succeed), Toast.LENGTH_LONG).show();
        _userNameChangePresenter.detachView();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.change_name_failed), Toast.LENGTH_LONG).show();
        _nameText.setText("");
    }
}
