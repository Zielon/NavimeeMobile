package org.pl.android.navimee.data.model;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Wojtek on 2017-10-21.
 */
@IgnoreExtraProperties
public  class Event implements Comparable<Event>, Serializable {

    private String id;
    private String attending_count;
    private String end_time;
    private String name;
    private String maybe_count;
    private String start_time;
    private String type;
    private Place place;

    public Event() {
    }

    public Event(String id, String attending_count, String end_time, String name, String maybe_count, String start_time, String type, Place place) {
        this.id = id;
        this.attending_count = attending_count;
        this.end_time = end_time;
        this.name = name;
        this.maybe_count = maybe_count;
        this.start_time = start_time;
        this.type = type;
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttending_count() {
        return attending_count;
    }

    public void setAttending_count(String attending_count) {
        this.attending_count = attending_count;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaybe_count() {
        return maybe_count;
    }

    public void setMaybe_count(String maybe_count) {
        this.maybe_count = maybe_count;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return 0;
    }
}
