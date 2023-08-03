package com.example.carpoolingapp;

/*
* This class is used to hold the information that will be used in My Rides
* @param activeCar provides the details of the driver,their ride and car details
* @param rideDetails provides details of the passenger
* */
public class MyRideInfo {
    ActiveCar activeCar;
    RideDetails rideDetails;
    MyRideInfo(){}

    public MyRideInfo(ActiveCar activeCar, RideDetails rideDetails) {
        this.activeCar = activeCar;
        this.rideDetails = rideDetails;
    }

    public ActiveCar getActiveCar() {
        return this.activeCar;
    }

    public void setActiveCar(ActiveCar activeCar) {
        this.activeCar = activeCar;
    }

    public RideDetails getRideDetails() {
        return this.rideDetails;
    }

    public void setRideDetails(RideDetails rideDetails) {
        this.rideDetails = rideDetails;
    }
}
