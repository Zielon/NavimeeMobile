package org.pl.android.drively.services;

import android.location.Location;

public class KalmanFilterService {
    private long time;
    private double latitude;
    private double longitude;
    private double variance = -1;
    private Location previousLocation;

    public void setState(Location location) {
        this.variance = Math.pow(location.getAccuracy(), 2);
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.time = location.getTime();
        this.previousLocation = location;
    }

    public Location filter(Location newLocation) {
        if (variance < 0) {
            // Initialise the state
            setState(newLocation);
        } else {
            long duration = newLocation.getTime() - this.time;
            if (duration > 0) {
                double speed = getSpeed(newLocation);
                variance += duration * speed * speed / 1000;
                time = newLocation.getTime();
            }

            double k = variance / (variance + newLocation.getAccuracy() * newLocation.getAccuracy());

            latitude += k * (newLocation.getLatitude() - latitude);
            longitude += k * (newLocation.getLongitude() - longitude);
            variance = (1 - k) * variance;

            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);

            this.previousLocation = newLocation;

            return new Location(newLocation);
        }

        return newLocation;
    }

    private double getSpeed(Location location) {
        if (previousLocation == null) return 0.0;
        double distance = location.distanceTo(previousLocation);
        long timeElapsed = location.getTime() - previousLocation.getTime();
        return distance / timeElapsed * 1000;
    }
}