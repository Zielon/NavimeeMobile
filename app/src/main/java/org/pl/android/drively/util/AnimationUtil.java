package org.pl.android.drively.util;

import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;


public class AnimationUtil {

    public static void animateFadeOutWithEndCallback(View view, EndCallback endCallback) {
        YoYo.with(Techniques.FadeOut)
                .duration(200)
                .repeat(0)
                .onEnd(animator -> {
                    view.setVisibility(View.GONE);
                    endCallback.onEnd();
                })
                .playOn(view);
    }

    public static void animateFlipIn(View view) {
        YoYo.with(Techniques.FlipInX)
                .duration(500)
                .repeat(0)
                .onStart(animator -> view.setVisibility(View.VISIBLE))
                .playOn(view);
    }

    @FunctionalInterface
    public interface EndCallback {
        void onEnd();
    }

    @FunctionalInterface
    public interface StartCallback {
        void onStartCallback();
    }

}
