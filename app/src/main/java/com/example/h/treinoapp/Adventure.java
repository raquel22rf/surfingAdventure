package com.example.h.treinoapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Adventure implements Serializable {
    private String advID;
    private String advName;
    private double latitude;
    private double longitude;
    private String advDescription;
    private Date advDate;
    private String advPlace;
    private List<Comment> comments;
    private String lat_Lng;
    private int durationHours, durationMinutes;
    private boolean hasCosts;
    private List<Object> participants;
    private int maxPeople;

    public Adventure(){

    }

    public Adventure(String advID, String advName, double latitude, double longitude, String advDescription, Date advDate, String advPlace, List comments, String lat_Lng, int durationHours, int durationMinutes, boolean hasCosts, List participants, int maxPeople) {
        this.advID = advID;
        this.advName = advName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.advDescription = advDescription;
        this.advDate = advDate;
        this.advPlace = advPlace;
        comments = new ArrayList<>();
        this.lat_Lng = lat_Lng;
        this.durationHours = durationHours;
        this.durationMinutes = durationMinutes;
        this.hasCosts = hasCosts;
        this.participants = new ArrayList<>();
        this.maxPeople = maxPeople;
    }



    public String getAdvID(){
        return advID;
    }

    public String getAdvName(){
        return advName;
    }

    public double getAdvLatitude() { return latitude; }

    public double getAdvLongitude() { return longitude; }

    public String getAdvDescription(){
        return  advDescription;
    }

    public Date getAdvDate(){
        return advDate;
    }

    public String getAdvPlace() { return advPlace; }

    public List getComments(){
        return comments;
    }

    public String getLat_Lng() { return lat_Lng; }

    public int getDurationHours() { return durationHours; }

    public int getDurationMinutes() { return durationMinutes; }

    public boolean getHasCosts() { return hasCosts; }

    public List getParticipants() { return participants; }

    public int getNumberParticipants() {
        //System.out.println("-------------------------> qual o numero?: " + participants.size());
        return participants.size();
    }

    public int getMaxPeople() { return maxPeople; }

    public void addParticipant(String id){
        participants.add(id);
    }

    public void removeParticipant(String id){
        participants.remove(id);
    }

}
