package org.pl.android.drively.ui.settings.user;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.drively.R;
import org.pl.android.drively.data.local.PreferencesHelper;
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

    @Inject
    PreferencesHelper _preferencesHelper;
    @BindView(R.id.avatar)
    AvatarView avatarView;
    @BindView(R.id.avatar_change)
    TextView avatarChangeText;
    @BindView(R.id.avatar_max_size)
    TextView avatarMaxSize;
    private Drawer _drawer = null;
    private Bundle _savedInstanceState;
    private int PICK_IMAGE_REQUEST = 1;
    private int CHANGE_SETTINGS = 2;
    private long FILE_MAX_SIZE_1_MB = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        activityComponent().inject(UserSettingsActivity.this);
        ButterKnife.bind(this);
        avatarChangeText.setVisibility(View.INVISIBLE);
        avatarMaxSize.setVisibility(View.INVISIBLE);

        _userSettingsPresenter.attachView(this);
        _savedInstanceState = savedInstanceState;

        loadAvatar();
        initDrawer();
    }

    private void loadAvatar() {
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(_userSettingsPresenter.getStorageReference(_preferencesHelper.getUserInfo().getAvatar()))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap avatar, GlideAnimation<? super Bitmap> glideAnimation) {
                        avatarView.destroyDrawingCache();
                        avatarView.refreshDrawableState();
                        avatarView.setImageBitmap(avatar);
                        avatarChangeText.setVisibility(View.VISIBLE);
                        avatarMaxSize.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null)
                if (checkSize(data.getData())) {
                    _userSettingsPresenter.setNewAvatar(data.getData(), _preferencesHelper.getUserInfo());
                } else
                    initDrawer();
            else
                _drawer.setSelection(-1);
    }

    private boolean checkSize(Uri returnUri) {
        Cursor returnCursor =
                getContentResolver().query(returnUri, null, null, null, null);

        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        if (returnCursor.getLong(sizeIndex) > FILE_MAX_SIZE_1_MB) {
            Toast.makeText(getBaseContext(), "The file is too big!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {
        Toast.makeText(getBaseContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void reloadAvatar() {
        avatarChangeText.setVisibility(View.INVISIBLE);
        avatarMaxSize.setVisibility(View.INVISIBLE);
        loadAvatar();
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
                    if (_userSettingsPresenter.isExternalProvider()) {
                        _drawer.setSelection(-1);
                    } else if (drawerItem instanceof Nameable) {
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