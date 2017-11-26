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
    private long attendingCount;
    private String endTime;
    private String name;
    private long maybeCount;
    private String startTime;
    private String type;
    private Place place;

    public Event() {
    }

    public Event(String id, long attendingCount, String endTime, String name, long maybeCount, String startTime, String type, Place place) {
        this.id = id;
        this.attendingCount = attendingCount;
        this.endTime = endTime;
        this.name = name;
        this.maybeCount = maybeCount;
        this.startTime = startTime;
        this.type = type;
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getattendingCount() {
        return attendingCount;
    }

    public void setattendingCount(long attendingCount) {
        this.attendingCount = attendingCount;
    }

    public String getendTime() {
        return endTime;
    }

    public void setendTime(String endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getmaybeCount() {
        return maybeCount;
    }

    public void setmaybeCount(long maybeCount) {
        this.maybeCount = maybeCount;
    }

    public String getstartTime() {
        return startTime;
    }

    public void setstartTime(String startTime) {
        this.startTime = startTime;
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
