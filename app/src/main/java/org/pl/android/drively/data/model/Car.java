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
    private double bearingEstimate;

    @JsonIgnore
    private double variance = -1;

    @JsonIgnore
    private GeoLocation currentLocation;

    @JsonIgnore
    private GeoLocation previousLocation;

    @JsonIgnore
    private int count = 0;

    @JsonIgnore
    private Marker marker;

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

    @Exclude
    public GeoLocation getCurrentLocation() {
        return currentLocation;
    }

    @Exclude
    public void setCurrentLocation(GeoLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Exclude
    public GeoLocation getPreviousLocation() {
        return previousLocation;
    }

    @Exclude
    public void setPreviousLocation(GeoLocation previousLocation) {
        this.previousLocation = previousLocation;
    }

    @Exclude
    public double getBearing() {
        double longitude1 = previousLocation.longitude;
        double longitude2 = currentLocation.longitude;
        double latitude1 = Math.toRadians(previousLocation.latitude);
        double latitude2 = Math.toRadians(currentLocation.latitude);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

        double ERROR_MEA = 50;

        // Reset. In the case of sudden movement changes. Otherwise a car's inertia is too big.
        if (count > 20) {
            variance = -1;
            count = 0;
        }

        if (variance < 0) {
            // Initial state
            bearingEstimate = bearing;
            variance = Math.pow(ERROR_MEA, 2);
        } else {
            double kalmanGain = variance / (variance + ERROR_MEA);
            bearingEstimate = bearingEstimate + kalmanGain * (bearing - bearingEstimate);
            variance = (1 - kalmanGain) * variance;
            count++;
        }

        return bearingEstimate;
    }
}
