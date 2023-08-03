package com.example.carpoolingapp;


/*
* This class would be used to hold the detais of the user including the passengerKey
* */
public class RideDetails {
    private JoinRideRequest joinRideRequest;
    private String passengerKey;
    public RideDetails(){}
    public RideDetails(JoinRideRequest joinRideRequest,String passengerKey){
        this.joinRideRequest=joinRideRequest;
        this.passengerKey=passengerKey;
    }
    public JoinRideRequest getJoinRideRequest(){
        return this.joinRideRequest;
    }
    public String getPassengerKey(){
        return this.passengerKey;
    }
}
