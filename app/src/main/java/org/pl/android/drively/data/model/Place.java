package org.pl.android.drively.data.model;



import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
/**
 * Created by Wojtek on 2017-10-22.
 */

@IgnoreExtraProperties
public class Place implements Serializable {
    private String id;
    private String address;
    private String name;
    private String category;
    GeoPoint geoPoint;
    private String city;

    public Place(String id, String address, String name, GeoPoint geoPoint, String city,String category) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.geoPoint = geoPoint;
        this.city = city;
        this.category = category;
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

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
