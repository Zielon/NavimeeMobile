package org.pl.android.navimee.data.model;



import java.io.Serializable;
/**
 * Created by Wojtek on 2017-10-22.
 */


public class Place implements Serializable {
    public  String fbId;
    public  String id;
    public  String category;
    public  String street;
    public  String name;
    public  Double longitude;
    public  Double latitude;
    public  String country;
    public  String city;

    public Place() {
    }

    public Place(String fbId, String id, String category, String street, String name, Double longitude, Double latitude, String country, String city) {
        this.fbId = fbId;
        this.id = id;
        this.category = category;
        this.street = street;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.city = city;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
