package org.pl.android.navimee.data.model;



import java.io.Serializable;
/**
 * Created by Wojtek on 2017-10-22.
 */


public class Place implements Serializable {
    public  String id;
    public  String name;
    public  Double longitude;
    public  Double latitude;
    public  String country;
    public  String zip;
    public Place() {
    }

    public Place(String id, String name, Double longitude, Double latitude, String country, String zip) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.zip = zip;
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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
