package org.pl.android.navimee.data.model;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import org.pl.android.navimee.util.Const;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Wojtek on 2017-10-21.
 */
@IgnoreExtraProperties
public  class Event implements Comparable<Event>, Serializable {

    private String id;
    private String title;
    private String description;
    private String category;
    private String timezone;
    private int rank;
    private Date startTime;
    private Date endTime;
    private Const.HotSpotType hotspotType;
    private Const.EventType source;
    private String firestoreId;
    private Place place;

    public Event() {
    }

    public Event(String id, String title, String description, String category, String timezone, int rank, GeoPoint geoPoint, Date startTime, Date endTime, Const.HotSpotType hotspotType, Const.EventType source,Place place) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.timezone = timezone;
        this.rank = rank;
        this.place = place;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hotspotType = hotspotType;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Const.HotSpotType getHotspotType() {
        return hotspotType;
    }

    public void setHotspotType(Const.HotSpotType hotspotType) {
        this.hotspotType = hotspotType;
    }

    public Const.EventType getSource() {
        return source;
    }

    public void setSource(Const.EventType source) {
        this.source = source;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public int compareTo(@NonNull Event event) {
        if(event.getEndTime().after(endTime)) {
            return -1;
        } else if(event.getEndTime().before(endTime)) {
            return 1;
        } else {
            if(event.getRank() > rank) {
                return 1;
            } else if(event.getRank() < rank) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return id != null ? id.equals(event.id) : event.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
