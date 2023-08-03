package com.example.carpoolingapp;

/*
* This interface is used to represent the Ride status
* Which can be either of the 4
* */
public interface RideStatus {
    String NONE=" ";
    String STARTED_RIDE="Started Ride";
    String JOINED_RIDE="Joined Ride";
    String REQUEST_SENT="Waiting for Response";
}
