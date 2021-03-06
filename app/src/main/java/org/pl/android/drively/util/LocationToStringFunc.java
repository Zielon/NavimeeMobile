package org.pl.android.drively.util;

/**
 * Created by Wojtek on 2017-10-28.
 */

import android.location.Location;

import io.reactivex.functions.Function;

public class LocationToStringFunc implements Function<Location, String> {
    @Override
    public String apply(Location location) {
        if (location != null)
            return location.getLatitude() + " " + location.getLongitude() + " (" + location.getAccuracy() + ")";
        return "no location available";
    }
}