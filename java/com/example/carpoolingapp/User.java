package com.example.carpoolingapp;

import java.io.Serializable;

/*
* This class is used to represent the details of the user
* */

public class User implements Serializable {
    private String email,name,contact,emergencyContact,age,gender,driverRef,activeCarRef,emergencyContactName,token,activeCarJoiningRef;
    private boolean isDriver,hasStartedDrive,hasJoinedDrive,hasSentJoiningRequest;
    String profilePicUrl;

    public String getProfilePicUrl() {
        return this.profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmergencyContact() {
        return this.emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyContactName() {
        return this.emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean getHasStartedDrive(){
        return this.hasStartedDrive;
    }
    public void setHasStartedDrive(boolean val){
        this.hasStartedDrive=val;
    }

    public boolean getHasJoinedDrive(){
        return this.hasJoinedDrive;
    }
    public void setHasJoinedDrive(boolean val){
        this.hasJoinedDrive=val;
    }


    public boolean getIsDriver(){
        return this.isDriver;
    }
    public void setIsDriver(boolean val){
        this.isDriver=val;
    }


    public String getDriverRef() {
        return this.driverRef;
    }

    public void setDriverRef(String driverRef) {
        this.driverRef = driverRef;
    }

    public String getActiveCarRef() {
        return this.activeCarRef;
    }

    public void setActiveCarRef(String activeCarRef) {
        this.activeCarRef = activeCarRef;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getActiveCarJoiningRef() {
        return activeCarJoiningRef;
    }

    public void setActiveCarJoiningRef(String activeCarJoiningRef) {
        this.activeCarJoiningRef = activeCarJoiningRef;
    }

    public boolean getHasSentJoiningRequest(){
        return this.hasSentJoiningRequest;
    }
    public void setHasSentJoiningRequest(boolean hasSentJoiningRequest){
        this.hasSentJoiningRequest = hasSentJoiningRequest;
    }

}
