package com.example.carpoolingapp;

/*
* This class holds all the information about the car
* */

public class CarDetails{
    private String carCompanyName="",carModelName="";
    private int totalNumberOfSeats;
    private String registrationNumber="";
    public CarDetails(){ }
    public CarDetails(String carCompanyName, String carModelName,int totalNumberOfSeats,String registrationNumber){
        this.carCompanyName=carCompanyName;
        this.carModelName=carModelName;
        this.totalNumberOfSeats=totalNumberOfSeats;
        this.registrationNumber=registrationNumber;
    }
    public String getCarCompanyName(){
        return this.carCompanyName;
    }
    public String getCarModelName(){
        return this.carModelName;
    }
    public int getTotalNumberOfSeats(){
        return this.totalNumberOfSeats;
    }
    public String getRegistrationNumber(){
        return this.registrationNumber;
    }
}
