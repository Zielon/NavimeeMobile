package org.pl.android.drively.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String email;
    private String id;
    private boolean online;
    private String name;
    private String token;
    private String city;
    private String country;

    //Settings
    private boolean dayScheduleNotification;
    private boolean bigEventsNotification;
    private boolean chatPrivateNotification;
    private boolean chatGroupNotification;
    private boolean shareLocalization;
    private String driverType;

    public User() {
        this.dayScheduleNotification = true;
        this.bigEventsNotification = true;
        this.chatGroupNotification = true;
        this.chatPrivateNotification = true;
        this.shareLocalization = false;
    }

    public boolean isDayScheduleNotification() {
        return dayScheduleNotification;
    }

    public void setDayScheduleNotification(boolean dayScheduleNotification) {
        this.dayScheduleNotification = dayScheduleNotification;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBigEventsNotification() {
        return bigEventsNotification;
    }

    public void setBigEventsNotification(boolean bigEventsNotification) {
        this.bigEventsNotification = bigEventsNotification;
    }

    public boolean isChatPrivateNotification() {
        return chatPrivateNotification;
    }

    public void setChatPrivateNotification(boolean chatPrivateNotification) {
        this.chatPrivateNotification = chatPrivateNotification;
    }

    public boolean isChatGroupNotification() {
        return chatGroupNotification;
    }

    public void setChatGroupNotification(boolean chatGroupNotification) {
        this.chatGroupNotification = chatGroupNotification;
    }

    public boolean isShareLocalization() {
        return shareLocalization;
    }

    public void setShareLocalization(boolean shareLocalization) {
        this.shareLocalization = shareLocalization;
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

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

}
