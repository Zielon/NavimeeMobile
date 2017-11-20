package org.pl.android.navimee;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.pl.android.navimee.ui.main.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    @Test
    public void clickBottomBar() {
        // Type text and then press the button.
        onView(withId(R.id.tab_events))
              .perform(click());

        onView(withId(R.id.tab_day_schedule))
                .perform(click());

        onView(withId(R.id.tab_hotspot))
                .perform(click());
    }


}
