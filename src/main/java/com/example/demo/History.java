package com.example.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class History {

    List<Location> locations ;

    private double distance;
    private int index=0;

    public double getDistance() {
        return Math.round(distance*1000.0)/1000.0;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public History() {
       locations=new LinkedList<>();
       distance=0;
    }

    public void addLocation(Location location){
        locations.add(location);
        if(index>0){
            distance+=Calculations.distanceInKilometers(locations.get(index-1).latitude,locations.get(index-1).longitude,
                    locations.get(index).latitude,locations.get(index).longitude);
        }
        index++;
    }
}
