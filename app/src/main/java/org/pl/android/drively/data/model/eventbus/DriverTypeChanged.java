package org.pl.android.drively.data.model.eventbus;

public class DriverTypeChanged {

    private String driverType;

    public DriverTypeChanged(String driverType) {
        this.driverType = driverType;
    }

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }
}
