package org.pl.android.drively.data.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.Exclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Car implements Comparable<Car> {

    private String userId;
    private String driverType;

    @JsonIgnore
    private Marker marker;

    @JsonIgnore
    private GeoLocation geoLocation;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    @Exclude
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    @Exclude
    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Exclude
    public Marker getMarker() {
        return marker;
    }

    @Exclude
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public int compareTo(@NonNull Car car) {
        return car.getUserId().compareTo(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        return userId != null ? userId.equals(car.userId) : car.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
