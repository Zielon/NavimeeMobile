package org.pl.android.drively.data.model;

import com.google.android.gms.maps.model.LatLng;

public class Feedback {
    private String userId;
    private String locationAddress;
    private String locationName;
    private int durationInSec;
    private int distanceValue;
    private LatLng geoPoint;
    private String date;

    public Feedback() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getDurationInSec() {
        return durationInSec;
    }

    public void setDurationInSec(int durationInSec) {
        this.durationInSec = durationInSec;
    }

    public int getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(int distanceValue) {
        this.distanceValue = distanceValue;
    }

    public LatLng getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(LatLng geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}


