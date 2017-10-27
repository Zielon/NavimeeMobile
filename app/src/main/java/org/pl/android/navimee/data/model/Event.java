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

    public  String fbId;
    public  String id;
    public  String startDate;
    public  Long maybe;
    public  String name;
    public  String endDate;
    public  Place place;
    public  String type;
    public  Long attending;
    public  String placeRefId;
    public  String pictureHttp;

    public Event() {
    }

    public Event(String fbId, String id, String startDate, Long maybe, String name, String endDate, Place place, String type, Long attending, String placeRefId, String pictureHttp) {
        this.fbId = fbId;
        this.id = id;
        this.startDate = startDate;
        this.maybe = maybe;
        this.name = name;
        this.endDate = endDate;
        this.place = place;
        this.type = type;
        this.attending = attending;
        this.placeRefId = placeRefId;
        this.pictureHttp = pictureHttp;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Long getMaybe() {
        return maybe;
    }

    public void setMaybe(Long maybe) {
        this.maybe = maybe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public Long getAttending() {
        return attending;
    }

    public void setAttending(Long attending) {
        this.attending = attending;
    }

    public String getPlaceRefId() {
        return placeRefId;
    }

    public void setPlaceRefId(String placeRefId) {
        this.placeRefId = placeRefId;
    }

    public String getPictureHttp() {
        return pictureHttp;
    }

    public void setPictureHttp(String pictureHttp) {
        this.pictureHttp = pictureHttp;
    }

    @Override
    public int compareTo(@NonNull Event another) {
        return fbId.compareToIgnoreCase(another.fbId);
    }
}
