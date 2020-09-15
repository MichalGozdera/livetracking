package com.example.demo;

import java.time.LocalDateTime;

public class Location {

    String locationDate;
    Double longitude;
    Double latitude;

    public String getLocationDate() {
        return locationDate;
    }

    public void setDate(String locationDate) {
        this.locationDate = locationDate;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Location(String locationDate, Double longitude, Double latitude) {
        this.locationDate = locationDate;
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
