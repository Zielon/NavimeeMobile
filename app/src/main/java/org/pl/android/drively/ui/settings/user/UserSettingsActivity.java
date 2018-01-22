package org.pl.android.drively.ui.settings.user;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.settings.user.email.UserEmailChangeActivity;
import org.pl.android.drively.ui.settings.user.name.UserNameChangeActivity;
import org.pl.android.drively.ui.settings.user.password.UserPasswordChangeActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class UserSettingsActivity extends BaseActivity implements UserSettingsChangeMvpView {

    @Inject
    UserSettingsPresenter _userSettingsPresenter;
    private Drawer _drawer = null;
    private Bundle _savedInstanceState;
    private int PICK_IMAGE_REQUEST = 1;
    private int CHANGE_SETTINGS = 2;

    @BindView(R.id.avatar)
    AvatarView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        activityComponent().inject(UserSettingsActivity.this);
        ButterKnife.bind(this);

        _userSettingsPresenter.attachView(this);
        _savedInstanceState = savedInstanceState;

        _userSettingsPresenter.getAvatarQuery().addOnSuccessListener(task -> {
            for (DocumentSnapshot document : task.getDocuments()) {
                User user = document.toObject(User.class);
                Glide.with(this)
                        .using(new FirebaseImageLoader())
                        .load(_userSettingsPresenter.getStorageReference(user.getAvatar()))
                        .into(avatarView);
                break;
            }
        });

        initDrawer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null)
                _userSettingsPresenter.setNewAvatar(data.getData());
            else
                initDrawer();
        else
            _drawer.setSelection(-1);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void reloadAvatar() {
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(_userSettingsPresenter.getStorageReference(""))
                .into(avatarView);
    }

    @OnClick(R.id.avatar)
    public void changeAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initDrawer() {

        Drawable grayBackground = getResources().getDrawable(R.drawable.primary);

        List<IDrawerItem> drawerItems = new ArrayList<>();

        drawerItems.add(new PrimaryDrawerItem().withName(_userSettingsPresenter.getName())
                .withIcon(R.drawable.happy_user_24dp)
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new PrimaryDrawerItem().withName(_userSettingsPresenter.getEmail())
                .withIcon(R.drawable.email_user_24dp)
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new PrimaryDrawerItem().withName("**********")
                .withIcon(R.drawable.password_24dp)
                .withTextColor(getResources().getColor(R.color.white)));

        _drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .addDrawerItems(drawerItems.toArray(new IDrawerItem[drawerItems.size()]))
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if(_userSettingsPresenter.isExternalProvider()){
                        _drawer.setSelection(-1);
                    }else if (drawerItem instanceof Nameable) {
                        Intent intent = null;
                        if (position == 0) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(UserSettingsActivity.this, UserNameChangeActivity.class);
                        } else if (position == 1) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(UserSettingsActivity.this, UserEmailChangeActivity.class);
                        } else if (position == 2) {
                            Timber.d(String.valueOf(position));
                            intent = new Intent(UserSettingsActivity.this, UserPasswordChangeActivity.class);
                        }

                        if (intent != null) {
                            UserSettingsActivity.this.startActivityForResult(intent, CHANGE_SETTINGS);
                        }
                    }
                    return false;
                })
                .withSavedInstance(_savedInstanceState)
                .withSelectedItem(-1)
                .buildView();

        _drawer.getSlider().setBackground(grayBackground);

        ViewGroup view = (ViewGroup) findViewById(R.id.frame_container);
        view.removeAllViews();
        view.addView(_drawer.getSlider());
    }
}