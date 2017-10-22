package org.pl.android.navimee.data.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Date;

/**
 * Created by Wojtek on 2017-10-22.
 */

@AutoValue
public abstract class Place implements Parcelable {
    public abstract  String fbId();
    public abstract  String id();
    public abstract  String category();
    @Nullable public abstract  String street();
    public abstract  String name();
    public abstract  String longitude();
    public abstract  String latitude();
    @Nullable public abstract  String country();
    @Nullable public abstract  String city();

    public static Place create(String fbId, String id, String category, String street, String name, String longitude, String latitude, String country, String city) {
        return new AutoValue_Place(fbId, id, category, street,name,longitude,latitude,country,city);
    }



    public static TypeAdapter<Place> typeAdapter(Gson gson) {
        return new AutoValue_Place.GsonTypeAdapter(gson);
    }

}
