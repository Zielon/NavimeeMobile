package org.pl.android.drively.data.model.eventbus;

public class HotspotSettingsChanged {

    private String driverType;

    private boolean shareLocalization;

    public HotspotSettingsChanged(String driverType, boolean shareLocalization) {
        this.driverType = driverType;
        this.shareLocalization = shareLocalization;
    }

    public String getDriverType() {
        return driverType;
    }

    public boolean getShareLocalization() {
        return shareLocalization;
    }

}
