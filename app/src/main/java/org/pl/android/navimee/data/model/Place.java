package org.pl.android.navimee.data.model;



import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
/**
 * Created by Wojtek on 2017-10-22.
 */

@IgnoreExtraProperties
public class Place implements Serializable {
    private String id;
    private String name;
    private String lon;
    private String lat;
    private String city;

    public Place(String id, String name, String lon, String lat, String city) {
        this.id = id;
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.city = city;
    }

    public Place() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
}
