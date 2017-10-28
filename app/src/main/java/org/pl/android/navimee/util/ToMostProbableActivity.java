package org.pl.android.navimee.util;

/**
 * Created by Wojtek on 2017-10-28.
 */

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import io.reactivex.functions.Function;

public class ToMostProbableActivity implements Function<ActivityRecognitionResult, DetectedActivity> {
    @Override
    public DetectedActivity apply(ActivityRecognitionResult activityRecognitionResult) {
        return activityRecognitionResult.getMostProbableActivity();
    }
}