package com.example.carpoolingapp;

import android.location.Address;


import java.io.Serializable;

/*
* This class is used to represent the String form of the address of the Location
* @param latlng represents the latitude-longitude combination
* @param address holds the address object consisting of all the details of the Address
* */

public class AddressDetails implements Serializable {
    private Address address;
    private LatLngDetails latLng;
    public AddressDetails(){ }
    public AddressDetails(Address address, LatLngDetails latLng){
        this.address=address;
        this.latLng=latLng;

    }
    public Address getAddress(){
        return this.address;
    }
    public LatLngDetails getLatLng(){
        return this.latLng;
    }
    public void setAddress(Address address){
        this.address = address;
    }
    public void setLatLng(LatLngDetails latLng){
        this.latLng = latLng;
    }

    @Override
    public String toString(){
        String addressLine = this.address.getAddressLine(0);
        return addressLine + " ; "+latLng.getLatitude() + " , "+ latLng.getLongitude();
    }
}
