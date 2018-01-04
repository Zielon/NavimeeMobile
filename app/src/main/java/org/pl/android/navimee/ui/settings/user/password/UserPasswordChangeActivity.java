package org.pl.android.navimee.ui.settings.user.password;

import android.os.Bundle;
import android.widget.Button;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserPasswordChangeActivity extends BaseActivity {

    @BindView(R.id.save)
    Button _saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_change);
        ButterKnife.bind(this);

    public void onClick(View v) {

        });
    }
}
