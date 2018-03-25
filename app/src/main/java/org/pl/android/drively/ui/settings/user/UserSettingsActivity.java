package org.pl.android.drively.ui.settings.user;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.pl.android.drively.R;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.pl.android.drively.util.ConstIntents.ACTION;
import static org.pl.android.drively.util.ConstIntents.DELETE_USER;

public class UserSettingsActivity extends BaseActivity implements UserSettingsChangeMvpView {

    @Inject
    UserSettingsPresenter userSettingsPresenter;
    @Inject
    PreferencesHelper preferencesHelper;
    @BindView(R.id.avatar)
    AvatarView avatarView;
    @BindView(R.id.avatar_change)
    TextView avatarChangeText;
    @BindView(R.id.avatar_max_size)
    TextView avatarMaxSize;
    @BindView(R.id.avatar_loader)
    ProgressBar avatarProgressBar;
    @BindView(R.id.avatar_layout)
    RelativeLayout avatarLayout;
    private Drawer drawer = null;
    private Bundle savedInstanceState;
    private int PICK_IMAGE_REQUEST = 1;
    private long FILE_MAX_SIZE_5_MB = 5000000;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        activityComponent().inject(UserSettingsActivity.this);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);

        avatarLayout.setVisibility(View.INVISIBLE);
        avatarProgressBar.setVisibility(View.VISIBLE);

        userSettingsPresenter.attachView(this);
        this.savedInstanceState = savedInstanceState;

        avatarView.setImageResource(R.drawable.default_avatar);
        userSettingsPresenter.loadAvatar();
        initDrawer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra(ACTION);
            if (action != null && action.equals(DELETE_USER)) showDeleteUserPopup();
            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                if (checkSize(data.getData())) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        if (bitmap == null) {
                            Toast.makeText(getBaseContext(), "An incorrect file!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        avatarLayout.setVisibility(View.INVISIBLE);
                        avatarProgressBar.setVisibility(View.VISIBLE);
                        ChatViewActivity.bitmapAvatarUser = bitmap;
                        userSettingsPresenter.setNewAvatar(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else
                initDrawer();
        }
        drawer.setSelection(-1);
    }

    private boolean checkSize(Uri returnUri) {
        Cursor returnCursor =
                getContentResolver().query(returnUri, null, null, null, null);

        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        if (returnCursor.getLong(sizeIndex) > FILE_MAX_SIZE_5_MB) {
            Toast.makeText(getBaseContext(), "The file is too big!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onSuccess() {
        avatarLayout.setVisibility(View.VISIBLE);
        avatarProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onError(Throwable throwable) {
        avatarLayout.setVisibility(View.VISIBLE);
        avatarProgressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(getBaseContext(), R.string.error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserDeleted() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void reloadAvatar(Bitmap avatar) {
        avatarView.setImageBitmap(avatar);
        avatarView.refreshDrawableState();
        onSuccess();
    }

    @OnClick(R.id.avatar_layout)
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

        drawerItems.add(new PrimaryDrawerItem().withName(userSettingsPresenter.getName())
                .withIcon(R.drawable.happy_user_24dp)
                .withSelectedColor(getResources().getColor(R.color.primary))
                .withSelectedTextColor(getResources().getColor(R.color.white))
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new PrimaryDrawerItem().withName(userSettingsPresenter.getEmail())
                .withIcon(R.drawable.email_user_24dp)
                .withSelectedColor(getResources().getColor(R.color.primary))
                .withSelectedTextColor(getResources().getColor(R.color.white))
                .withTextColor(getResources().getColor(R.color.white)));

        drawerItems.add(new DividerDrawerItem().withEnabled(true));

        drawerItems.add(new PrimaryDrawerItem().withName(R.string.delete_account)
                .withIcon(R.drawable.close_x_24dp)
                .withSelectedColor(getResources().getColor(R.color.primary))
                .withSelectedTextColor(getResources().getColor(R.color.white))
                .withTextColor(getResources().getColor(R.color.white)));

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .addDrawerItems(drawerItems.toArray(new IDrawerItem[drawerItems.size()]))
                .withSliderBackgroundColor(0)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (position < 3) {
                        drawer.setSelection(-1);
                    } else if (drawerItem instanceof Nameable) {
                        if (position == 3) showDeleteUserPopup();
                    }
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(-1)
                .buildView();

        drawer.getSlider().setBackground(grayBackground);

        ViewGroup view = (ViewGroup) findViewById(R.id.frame_container);
        view.removeAllViews();
        view.addView(drawer.getSlider());
    }

    private void showDeleteUserPopup() {
        new MaterialDialog.Builder(this)
                .backgroundColor(ContextCompat.getColor(this, R.color.md_red_800))
                .positiveColor(ContextCompat.getColor(this, R.color.white))
                .negativeColor(ContextCompat.getColor(this, R.color.white))
                .contentColor(ContextCompat.getColor(this, R.color.white))
                .positiveText(R.string.delete_user_popup_positive)
                .negativeText(R.string.delete_user_popup_negative)
                .content(R.string.delete_user_popup_content)
                .dismissListener(dialog -> drawer.setSelection(-1))
                .onNegative((dialog, which) -> dialog.dismiss())
                .onPositive((dialog, which) -> {
                    progressDialog.setMessage(getResources().getString(R.string.delete_user_deleting));
                    progressDialog.show();
                    userSettingsPresenter.deleteUser(progressDialog);
                }).show();
    }
}