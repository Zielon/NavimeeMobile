package org.pl.android.drively.ui.hotspot;

import com.firebase.geofire.GeoQueryDataEventListener;

public abstract class BaseGeoFireListener implements GeoQueryDataEventListener {
    private String NAME;

    public String getName() {
        return NAME;
    }

    public void setName(String name) {
        this.NAME = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BaseGeoFireListener listener = (BaseGeoFireListener) object;
        return listener.getName().equals(NAME);
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }
}
