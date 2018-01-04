package org.pl.android.navimee.ui.settings.user.name;

import android.os.Bundle;
import android.widget.Button;

import org.pl.android.navimee.R;
import org.pl.android.navimee.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserNameChangeActivity extends BaseActivity {

    @BindView(R.id.save)
    Button _saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        ButterKnife.bind(this);

        _saveButton.setOnClickListener(v -> {

        });
    }
}
