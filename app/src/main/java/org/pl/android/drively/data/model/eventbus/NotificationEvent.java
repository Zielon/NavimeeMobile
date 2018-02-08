package org.pl.android.drively.data.model.eventbus;

/**
 * Created by Wojtek on 2018-02-08.
 */

public class NotificationEvent {
    private double lat;
    private double lng;
    private String name;
    private String count;

    public NotificationEvent(double lat, double lng, String name, String count) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.count = count;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
