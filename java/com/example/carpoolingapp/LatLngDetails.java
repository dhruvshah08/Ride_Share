package com.example.carpoolingapp;

import java.io.Serializable;

/*
* This class is used to represent the latitude and longitude of a location on the map
* */
public class LatLngDetails implements Serializable {
    private double latitude,longitude;
    public LatLngDetails(){

    }
    public LatLngDetails(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String toString(){
        return this.latitude + ","+this.longitude;
    }
}
