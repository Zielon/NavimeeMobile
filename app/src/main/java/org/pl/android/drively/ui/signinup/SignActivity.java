package org.pl.android.drively.ui.signinup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.drively.R;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

import static org.pl.android.drively.util.ConstIntents.ACTION;
import static org.pl.android.drively.util.ConstIntents.DELETE_USER;
import static org.pl.android.drively.util.ConstIntents.PROVIDERS;
import static org.pl.android.drively.util.ConstIntents.REAUTHENTICATE;

public class SignActivity extends BaseSignActivity implements BaseSignMvpView {

    @Inject
    SignPresenter mSignPresenter;

    @BindView(R.id.title)
    TextView titleTextView;

    @BindView(R.id.login_buttons_label)
    TextView buttonLabel;

    @BindView(R.id.regulation)
    TextView regulation;

    @BindView(R.id.facebook_com)
    FancyButton facebookReplacedButton;

    @BindView(R.id.facebook_com_icon)
    FancyButton facebookReplacedButtonIcon;

    @BindView(R.id.google_com)
    FancyButton googleReplacedButton;

    @BindView(R.id.google_com_icon)
    FancyButton googleReplacedButtonIcon;

    private boolean reauthenticate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_sign);
        ButterKnife.bind(this);
        mSignPresenter.attachView(this);
        progressDialog.setMessage(getResources().getString(R.string.login_progress));
        titleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "NexaBold.ttf"));
        initializeSocialButtons();

        Map<String, Pair<FancyButton, FancyButton>> buttons = new HashMap<>();

        buttons.put("google.com", new Pair<>(googleReplacedButton, googleReplacedButtonIcon));
        buttons.put("facebook.com", new Pair<>(facebookReplacedButton, facebookReplacedButtonIcon));

        // In the case of reauthentication from the delete account menu
        String action = getIntent().getStringExtra(ACTION);
        if(action != null && action.equals(REAUTHENTICATE)){
            buttonLabel.setText(R.string.reauthenticate);
            regulation.setVisibility(View.GONE);
            reauthenticate = true;

            List<String> providers = getIntent().getStringArrayListExtra(PROVIDERS);
            List<String> availableButtons = new ArrayList<>(buttons.keySet());
            Stream.of(ListUtils.subtract(availableButtons, providers))
                    .forEach(provider -> {
                        Pair<FancyButton, FancyButton> pair = buttons.get(provider);
                        pair.first.setVisibility(View.GONE);
                        pair.second.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    protected void onErrorFacebook() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.register_failed), Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    @Override
    protected void loginInWithFacebookOrGoogle(AuthCredential credential) {
        mSignPresenter.loginInWithFacebookOrGoogle(credential);
    }

    @Override
    public void onSuccess() {
        if(reauthenticate){
            progressDialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra(ACTION, DELETE_USER);
            setResult(RESULT_OK, intent);
            this.finish();
            return;
        }

        mSignPresenter.registerUser().addOnCompleteListener(user -> {
            progressDialog.dismiss();
            setResult(RESULT_OK, null);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onError(Throwable throwable) {
        progressDialog.dismiss();
        if (throwable instanceof FirebaseAuthUserCollisionException)
            Toast.makeText(getBaseContext(), getResources().getString(R.string.emailAlreadyInUse), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.google_com)
    public void replacedSignInGoogleButtonClick(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.facebook_com)
    public void replacedFacebookLoginButtonClick(View view) {
        facebookButton.performClick();
    }
}