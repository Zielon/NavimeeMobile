package org.pl.android.navimee.data.model;

import java.io.Serializable;

/**
 * Created by Wojtek on 2017-10-30.
 */

public class Date implements Serializable {
    Integer date;
    Integer day;
    Integer hours;
    Integer minutes;
    Integer month;
    Integer seconds;
    Long time;
    Integer timezoneOffset;
    Integer year;

    public Date() {
    }

    public Date(Integer date, Integer day, Integer hours, Integer minutes, Integer month, Integer seconds, Long time, Integer timezoneOffset, Integer year) {
        this.date = date;
        this.day = day;
        this.hours = hours;
        this.minutes = minutes;
        this.month = month;
        this.seconds = seconds;
        this.time = time;
        this.timezoneOffset = timezoneOffset;
        this.year = year;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(Integer timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

