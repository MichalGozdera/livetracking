package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class History {

    List<Location> locations ;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public History() {
       locations=new ArrayList<>();
    }

    public void addLocation(Location location){
        locations.add(location);
    }
}
