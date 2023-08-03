package com.example.carpoolingapp;

import java.util.ArrayList;

/*
* This class contains all the information of the Driver including the key to it's information in the User's collection
*
* */

public class ActiveCar {
    private CarDetails carDetails;
    private String driverKey;
    private ArrayList<String> gendersComfortableWith;
    private String startTime;
    private String sourceAddress;
    private String destinationAddress;
    private int numberOfFreeSeats;
    private String currentSubLocality;
    private String currentLatLng;
    public ActiveCar(){ }

    public ActiveCar(CarDetails carDetails, String driverKey, ArrayList<String> gendersComfortableWith, String startTime, String sourceAddress, String destinationAddress, int numberOfFreeSeats, String currentSubLocality, String currentLatLng){
        this.carDetails =carDetails;
        this.driverKey=driverKey;
        this.gendersComfortableWith=gendersComfortableWith;
        this.startTime=startTime;
        this.sourceAddress=sourceAddress;
        this.destinationAddress=destinationAddress;
        this.numberOfFreeSeats=numberOfFreeSeats;
        this.currentSubLocality=currentSubLocality;
        this.currentLatLng=currentLatLng;
    }

    public CarDetails getCarDetails() {
        return carDetails;
    }

    public void setCarDetails(CarDetails carDetails) {
        this.carDetails = carDetails;
    }

    public String getDriverKey() {
        return driverKey;
    }

    public void setDriverKey(String driverKey) {
        this.driverKey = driverKey;
    }

    public ArrayList<String> getGendersComfortableWith() {
        return gendersComfortableWith;
    }

    public void setGendersComfortableWith(ArrayList<String> gendersComfortableWith) {
        this.gendersComfortableWith = gendersComfortableWith;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getNumberOfFreeSeats() {
        return numberOfFreeSeats;
    }

    public void setNumberOfFreeSeats(int numberOfFreeSeats) {
        this.numberOfFreeSeats = numberOfFreeSeats;
    }

    public String getCurrentSubLocality() {
        return currentSubLocality;
    }

    public void setCurrentSubLocality(String currentSubLocality) {
        this.currentSubLocality = currentSubLocality;
    }

    public String getCurrentLatLng() {
        return currentLatLng;
    }

    public void setCurrentLatLng(String currentLatLng) {
        this.currentLatLng = currentLatLng;
    }
}
