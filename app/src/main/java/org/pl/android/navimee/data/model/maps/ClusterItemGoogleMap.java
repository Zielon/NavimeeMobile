package org.pl.android.navimee.data.model.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by wojciech.grazawski on 2017-11-30.
 */

public class ClusterItemGoogleMap implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    String id;

    public ClusterItemGoogleMap(String id,double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        this.id = id;
        mTitle = null;
        mSnippet = null;
    }

    public ClusterItemGoogleMap(String id,double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        this.id = id;
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() { return mTitle; }


    public String getSnippet() { return mSnippet; }

    /**
     * Set the title of the marker
     * @param title string to be set as title
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
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

