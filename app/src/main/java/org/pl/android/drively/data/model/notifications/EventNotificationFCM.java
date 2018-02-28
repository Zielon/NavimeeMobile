package org.pl.android.drively.data.model.notifications;

public class EventNotificationFCM extends NotificationFCM {
    private String title;
    private String endTime;
    private double lat;
    private double lon;

    public EventNotificationFCM() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
