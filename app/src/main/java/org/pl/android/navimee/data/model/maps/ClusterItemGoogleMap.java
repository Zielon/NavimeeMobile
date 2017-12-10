package org.pl.android.navimee.data.model.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by wojciech.grazawski on 2017-11-30.
 */

public class ClusterItemGoogleMap implements ClusterItem {
    public final LatLng mPosition;
    public  int profilePhoto;
    public  String name;
    String id;

    public ClusterItemGoogleMap(String id, LatLng position, String name, int pictureResource) {
        this.id = id;
        mPosition = position;
        this.name = name;
        profilePhoto = pictureResource;
    }


    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public int getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(int profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterItemGoogleMap that = (ClusterItemGoogleMap) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

