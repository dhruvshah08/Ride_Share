package com.example.carpoolingapp;


/*
* This class is used to represent the details of the user who will be requesting to join the ride
* The properties of ridejoiningTime and distanceAwayToCarSelected will be set when the car for the ride is selected!
* */

public class JoinRideRequest {
    private String sourceAddress,destinationAddress;
    private LatLngDetails sourceLatLng,destLatLng;
    private int numberOfSeats;
    private String ridejoiningTime="";
    private double distanceAwayToCarSelected;

    public JoinRideRequest(){

    }
    public JoinRideRequest(String sourceAddress, String destinationAddress, LatLngDetails sourceLatLng, LatLngDetails destLatLng, int numberOfSeats){
        this.sourceAddress = sourceAddress;
        this.destinationAddress= destinationAddress;
        this.sourceLatLng = sourceLatLng;
        this.destLatLng = destLatLng;
        this.numberOfSeats = numberOfSeats;
    }
    public String getSourceAddress(){
        return this.sourceAddress;
    }
    public String getDestinationAddress(){
        return this.destinationAddress;
    }
    public int getNumberOfSeats(){
        return this.numberOfSeats;
    }
    public LatLngDetails getSourceLatLng(){
        return this.sourceLatLng;
    }
    public LatLngDetails getDestLatLng(){
        return this.destLatLng;
    }
    public String getRidejoiningTime() {
        return this.ridejoiningTime;
    }

    public void setRidejoiningTime(String ridejoiningTime) {
        this.ridejoiningTime = ridejoiningTime;
    }
    public double getDistanceAwayToCarSelected() {
        return this.distanceAwayToCarSelected;
    }

    public void setDistanceAwayToCarSelected(double distanceAwayToCarSelected) {
        this.distanceAwayToCarSelected = distanceAwayToCarSelected;
    }
}


//package com.example.carpoolingapp;
//
//import com.google.android.gms.maps.model.LatLng;
//
//public class JoinRideRequest {
//    private String sourceAddress,destinationAddress;
//    private LatLngDetails sourceLatLng,destLatLng;
//    private int numberOfSeats;
//    private double distanceAway;
//    private String joinRideTime="";
//    public JoinRideRequest(String sourceAddress,String destinationAddress,LatLngDetails sourceLatLng,LatLngDetails destLatLng,int numberOfSeats,double distanceAway){
//        this.sourceAddress = sourceAddress;
//        this.destinationAddress= destinationAddress;
//        this.sourceLatLng = sourceLatLng;
//        this.destLatLng = destLatLng;
//        this.numberOfSeats = numberOfSeats;
//        this.distanceAway = distanceAway;
//    }
//    public String getSourceAddress(){
//        return this.sourceAddress;
//    }
//    public String getDestinationAddress(){
//        return this.destinationAddress;
//    }
//    public int getNumberOfSeats(){
//        return this.numberOfSeats;
//    }
//    public LatLngDetails getSourceLatLng(){
//        return this.sourceLatLng;
//    }
//    public LatLngDetails getDestLatLng(){
//        return this.destLatLng;
//    }
//    public Double getDistanceAway(){
//        return this.distanceAway;
//    }
//
//    public String getJoinRideTime() {
//        return joinRideTime;
//    }
//
//    public void setJoinRideTime(String joinRideTime) {
//        this.joinRideTime = joinRideTime;
//    }
//}

