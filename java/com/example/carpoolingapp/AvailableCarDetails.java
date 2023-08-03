package com.example.carpoolingapp;

/*
 *This class is used for the rendering of information to display to the user for selecting the ride
 * This information is rendered in the MyListAdapter
 *  */
public class AvailableCarDetails {
    private ActiveCar activeCar;
    double distanceAway;
    String activeCarRef;

    public AvailableCarDetails(){
    }
    public AvailableCarDetails(ActiveCar activeCar, double distanceAway,String activeCarRef) {
        this.activeCar = activeCar;
        this.activeCarRef=activeCarRef;
        this.distanceAway = distanceAway;
    }

    public ActiveCar getActiveCar() {
        return this.activeCar;
    }

    public void setActiveCar(ActiveCar activeCar) {
        this.activeCar = activeCar;
    }

    public double getDistanceAway() {
        return this.distanceAway;
    }

    public String getActiveCarRef() {
        return this.activeCarRef;
    }

    public void setActiveCarRef(String activeCarRef) {
        this.activeCarRef = activeCarRef;
    }

    public void setDistanceAway(double distanceAway) {
        this.distanceAway = distanceAway;
    }
}