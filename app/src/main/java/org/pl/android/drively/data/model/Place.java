package org.pl.android.drively.data.model;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Place implements Serializable {
    double lat;
    double lon;
    private String id;
    private String address;
    private String name;
    private String category;
    private String city;

    public Place(String id, String address, String name, String category, String city, double lat, double lon) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.category = category;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
