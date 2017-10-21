package org.pl.android.navimee.data.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Created by Wojtek on 2017-10-21.
 */
@AutoValue
public abstract class Event implements Comparable<Event>, Parcelable {

    public abstract Profile profile();

    public static Event create(Profile profile) {
        return new AutoValue_Event(profile);
    }

    public static TypeAdapter<Event> typeAdapter(Gson gson) {
        return new AutoValue_Event.GsonTypeAdapter(gson);
    }

    @Override
    public int compareTo(@NonNull Event another) {
        return profile().name().first().compareToIgnoreCase(another.profile().name().first());
    }
}
