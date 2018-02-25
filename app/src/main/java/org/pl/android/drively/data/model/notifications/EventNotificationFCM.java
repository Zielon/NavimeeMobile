package org.pl.android.drively.data.model.notifications;


public class EventNotificationFCM extends NotificationFCM {
    private String title;
    private String endTime;
    private String lat;
    private String lng;

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

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
