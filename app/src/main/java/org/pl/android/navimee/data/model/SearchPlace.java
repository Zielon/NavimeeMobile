package org.pl.android.navimee.data.model;

/**
 * Created by Wojtek on 2017-11-19.
 */

public class SearchPlace {
    private String id;
    private String lon;
    private String name;
    private String lat;
    private String city;

    public SearchPlace(String id, String lon, String name, String lat, String city) {
        this.id = id;
        this.lon = lon;
        this.name = name;
        this.lat = lat;
        this.city = city;
    }

    public SearchPlace() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
