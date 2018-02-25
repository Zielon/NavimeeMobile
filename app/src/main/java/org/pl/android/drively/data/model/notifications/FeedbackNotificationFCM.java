package org.pl.android.drively.data.model.notifications;


public class FeedbackNotificationFCM extends NotificationFCM {
    private String locationName;
    private String name;
    private String locationAddress;
    private String id;

    public FeedbackNotificationFCM() {
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
