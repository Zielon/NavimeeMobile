package org.pl.android.navimee.data.model;

import java.io.Serializable;

/**
 * Created by Wojtek on 2017-12-12.
 */

public class FourSquarePlace implements Serializable {
    private String id;
    private String name;
    private double rating;

    private int likesCount;
    private String likesSummary;

    private int statsCheckinsCount;
    private int statsUsersCount;
    private int statsTipCount;
    private int statsVisitsCount;

    private String locationAddress;
    private String locationCrossStreet;
    private String locationPostalCode;
    private String locationCity;
    private String locationCountry;
    private double locationLat;
    private double locationLng;

    private String popularStatus;
    private boolean popularIsOpen;
    private boolean popularIsLocalHoliday;

    public FourSquarePlace() {
    }

    public FourSquarePlace(String id, String name, double rating, int likesCount, String likesSummary, int statsCheckinsCount, int statsUsersCount, int statsTipCount, int statsVisitsCount, String locationAddress, String locationCrossStreet, String locationPostalCode, String locationCity, String locationCountry, double locationLat, double locationLng, String popularStatus, boolean popularIsOpen, boolean popularIsLocalHoliday) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.likesCount = likesCount;
        this.likesSummary = likesSummary;
        this.statsCheckinsCount = statsCheckinsCount;
        this.statsUsersCount = statsUsersCount;
        this.statsTipCount = statsTipCount;
        this.statsVisitsCount = statsVisitsCount;
        this.locationAddress = locationAddress;
        this.locationCrossStreet = locationCrossStreet;
        this.locationPostalCode = locationPostalCode;
        this.locationCity = locationCity;
        this.locationCountry = locationCountry;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.popularStatus = popularStatus;
        this.popularIsOpen = popularIsOpen;
        this.popularIsLocalHoliday = popularIsLocalHoliday;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getLikesSummary() {
        return likesSummary;
    }

    public void setLikesSummary(String likesSummary) {
        this.likesSummary = likesSummary;
    }

    public int getStatsCheckinsCount() {
        return statsCheckinsCount;
    }

    public void setStatsCheckinsCount(int statsCheckinsCount) {
        this.statsCheckinsCount = statsCheckinsCount;
    }

    public int getStatsUsersCount() {
        return statsUsersCount;
    }

    public void setStatsUsersCount(int statsUsersCount) {
        this.statsUsersCount = statsUsersCount;
    }

    public int getStatsTipCount() {
        return statsTipCount;
    }

    public void setStatsTipCount(int statsTipCount) {
        this.statsTipCount = statsTipCount;
    }

    public int getStatsVisitsCount() {
        return statsVisitsCount;
    }

    public void setStatsVisitsCount(int statsVisitsCount) {
        this.statsVisitsCount = statsVisitsCount;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getLocationCrossStreet() {
        return locationCrossStreet;
    }

    public void setLocationCrossStreet(String locationCrossStreet) {
        this.locationCrossStreet = locationCrossStreet;
    }

    public String getLocationPostalCode() {
        return locationPostalCode;
    }

    public void setLocationPostalCode(String locationPostalCode) {
        this.locationPostalCode = locationPostalCode;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public void setLocationCountry(String locationCountry) {
        this.locationCountry = locationCountry;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public String getPopularStatus() {
        return popularStatus;
    }

    public void setPopularStatus(String popularStatus) {
        this.popularStatus = popularStatus;
    }

    public boolean isPopularIsOpen() {
        return popularIsOpen;
    }

    public void setPopularIsOpen(boolean popularIsOpen) {
        this.popularIsOpen = popularIsOpen;
    }

    public boolean isPopularIsLocalHoliday() {
        return popularIsLocalHoliday;
    }

    public void setPopularIsLocalHoliday(boolean popularIsLocalHoliday) {
        this.popularIsLocalHoliday = popularIsLocalHoliday;
    }
}
