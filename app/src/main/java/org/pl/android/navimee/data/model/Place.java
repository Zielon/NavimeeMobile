package org.pl.android.navimee.data.model;



import java.io.Serializable;
/**
 * Created by Wojtek on 2017-10-22.
 */


public class Place implements Serializable {
    private String id;
    private String name;
    private String longitude;
    private String latitude;
    private String city;
    private String country;

    public Place(String id, String name, String longitude, String latitude, String city, String country) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.city = city;
        this.country = country;
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

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
