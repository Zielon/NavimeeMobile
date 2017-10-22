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

    public abstract String fbId();
    public abstract String id();
    public abstract String startDate();
    public abstract String maybe();
    public abstract String name();
    public abstract String endDate();
    public abstract Place place();
    public abstract String type();
    public abstract String attending();
    public abstract String placeRefId();
    public abstract String pictureHttp();

    public static Event create(String fbId, String id, String startDate, String maybe, String name, String endDate, Place place, String type, String attending, String placeRefId, String pictureHttp) {
        return new AutoValue_Event(fbId,id,startDate,maybe,name,endDate,place,type,attending,placeRefId,pictureHttp);
    }

    public static TypeAdapter<Event> typeAdapter(Gson gson) {
        return new AutoValue_Event.GsonTypeAdapter(gson);
    }

    @Override
    public int compareTo(@NonNull Event another) {
        return id().compareToIgnoreCase(another.id());
    }
}
