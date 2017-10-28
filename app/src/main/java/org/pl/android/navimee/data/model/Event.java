package org.pl.android.navimee.data.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.Serializable;

/**
 * Created by Wojtek on 2017-10-21.
 */

public  class Event implements Comparable<Event>, Serializable {
    
    public  String id;
    public  String start_time;
    public  Long maybe_count;
    public  String name;
    public  String end_time;
    public  Place place;
    public  String type;
    public  Long attending_count;


    public Event() {
    }

    public Event(String id, String start_time, Long maybe_count, String name, String end_time, Place place, String type, Long attending_count) {
        this.id = id;
        this.start_time = start_time;
        this.maybe_count = maybe_count;
        this.name = name;
        this.end_time = end_time;
        this.place = place;
        this.type = type;
        this.attending_count = attending_count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public Long getMaybe_count() {
        return maybe_count;
    }

    public void setMaybe_count(Long maybe_count) {
        this.maybe_count = maybe_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAttending_count() {
        return attending_count;
    }

    public void setAttending_count(Long attending_count) {
        this.attending_count = attending_count;
    }

    @Override
    public int compareTo(@NonNull Event another) {
        return attending_count.compareTo(another.attending_count);
    }
}
