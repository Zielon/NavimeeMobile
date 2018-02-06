package org.pl.android.drively.ui.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import org.pl.android.drively.R;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        getSupportActionBar().setElevation(0);

        SliderPage firstFragment = new SliderPage();
        firstFragment.setTitle(getResources().getString(R.string.onboarding_1_title));
        firstFragment.setDescription(getResources().getString(R.string.onboarding_1_body));
        firstFragment.setImageDrawable(R.drawable.onboarding_1_welcome);
        firstFragment.setBgColor(getBaseContext().getResources().getColor(R.color.primary));


        SliderPage secondFragment = new SliderPage();
        secondFragment.setTitle(getResources().getString(R.string.onboarding_2_title));
        secondFragment.setDescription(getResources().getString(R.string.onboarding_2_body));
        secondFragment.setImageDrawable(R.drawable.onboarding_events_24dp);
        secondFragment.setBgColor(getBaseContext().getResources().getColor(R.color.primary));

        SliderPage thirdFragment = new SliderPage();
        thirdFragment.setTitle(getResources().getString(R.string.onboarding_3_title));
        thirdFragment.setDescription(getResources().getString(R.string.onboarding_3_body));
        thirdFragment.setImageDrawable(R.drawable.onboarding_schedule_24dp);
        thirdFragment.setBgColor(getBaseContext().getResources().getColor(R.color.primary));

        SliderPage fourthFragment = new SliderPage();
        fourthFragment.setTitle(getResources().getString(R.string.onboarding_4_title));
        fourthFragment.setDescription(getResources().getString(R.string.onboarding_4_body));
        fourthFragment.setImageDrawable(R.drawable.onboarding_hotspot_24dp);
        fourthFragment.setBgColor(getBaseContext().getResources().getColor(R.color.primary));

        SliderPage fifthFragment = new SliderPage();
        fifthFragment.setTitle(getResources().getString(R.string.onboarding_5_title));
        fifthFragment.setDescription(getResources().getString(R.string.onboarding_5_body));
        fifthFragment.setImageDrawable(R.drawable.onboarding_chat_24dp);
        fifthFragment.setBgColor(getBaseContext().getResources().getColor(R.color.primary));
        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(AppIntroFragment.newInstance(firstFragment));
        addSlide(AppIntroFragment.newInstance(secondFragment));
        addSlide(AppIntroFragment.newInstance(thirdFragment));
        addSlide(AppIntroFragment.newInstance(fourthFragment));
        addSlide(AppIntroFragment.newInstance(fifthFragment));



        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#242832"));
        //setSeparatorColor(Color.parseColor("#242832"));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}