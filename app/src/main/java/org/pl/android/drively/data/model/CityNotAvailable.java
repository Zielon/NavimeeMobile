package org.pl.android.drively.data.model;


public class CityNotAvailable {
    private String city;
    private String countryName;
    private int count;

    public CityNotAvailable() {
        this.count = 1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
