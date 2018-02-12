package org.pl.android.drively.data.model;

import android.support.annotation.NonNull;

import org.pl.android.drively.util.Const;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Wojtek on 2018-02-12.
 */

public class EventNotification implements Comparable<EventNotification>, Serializable {

    private String id;
    private String title;
    private String userId;
    private int rank;
    private Date startTime;
    private Date endTime;
    private Const.HotSpotType hotspotType;
    private boolean isSent;
    private Place place;


    public EventNotification() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
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

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public int compareTo(@NonNull EventNotification event) {
        if (event.getEndTime().after(endTime)) {
            return -1;
        } else if (event.getEndTime().before(endTime)) {
            return 1;
        } else {
            if (event.getRank() > rank) {
                return 1;
            } else if (event.getRank() < rank) {
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

        EventNotification event = (EventNotification) o;

        return id != null ? id.equals(event.id) : event.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
