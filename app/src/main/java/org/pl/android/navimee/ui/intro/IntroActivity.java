package org.pl.android.navimee.ui.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import org.pl.android.navimee.R;

/**
 * Created by Wojtek on 2017-10-20.
 */

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        SliderPage firstFragment = new SliderPage();
        firstFragment.setTitle("Welcome!");
        firstFragment.setDescription("This is a demo of the AppIntro library.");
        firstFragment.setImageDrawable(R.drawable.ic_slide1);
        firstFragment.setBgColor(Color.TRANSPARENT);


        SliderPage secondFragment = new SliderPage();
        secondFragment.setTitle("Clean App Intros");
        secondFragment.setDescription("This library offers developers the ability to add clean app intros at the start of their apps.");
        secondFragment.setImageDrawable(R.drawable.ic_slide2);
        secondFragment.setBgColor(Color.TRANSPARENT);

        SliderPage thirdFragment = new SliderPage();
        thirdFragment.setTitle("Simple, yet Customizable");
        thirdFragment.setDescription("The library offers a lot of customization, while keeping it simple for those that like simple.");
        thirdFragment.setImageDrawable(R.drawable.ic_slide3);
        thirdFragment.setBgColor(Color.TRANSPARENT);

        SliderPage fourthFragment = new SliderPage();
        fourthFragment.setTitle("Explore");
        fourthFragment.setDescription("Feel free to explore the rest of the library demo!");
        fourthFragment.setImageDrawable(R.drawable.ic_slide4);
        fourthFragment.setBgColor(Color.TRANSPARENT);
        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(AppIntroFragment.newInstance(firstFragment));
        addSlide(AppIntroFragment.newInstance(secondFragment));
        addSlide(AppIntroFragment.newInstance(thirdFragment));
        addSlide(AppIntroFragment.newInstance(fourthFragment));


        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
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