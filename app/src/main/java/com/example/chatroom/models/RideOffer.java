package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;

public class RideOffer implements Serializable {

    public ArrayList<String> rider;
    public ArrayList<String> offeror;
    String ride_id, offer_id;
    ArrayList<Double> location;

    public RideOffer(String ride_id, ArrayList<String> rider, ArrayList<String> offeror, ArrayList<Double> location) {
        this.ride_id = ride_id;
        this.rider = rider;
        this.offeror = offeror;
        this.location = location;
    }

    public RideOffer() {
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public ArrayList<String> getRider() {
        return rider;
    }

    public void setRider(ArrayList<String> rider) {
        this.rider = rider;
    }

    public ArrayList<String> getOfferor() {
        return offeror;
    }

    public void setOfferor(ArrayList<String> offeror) {
        this.offeror = offeror;
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public String getRiderId() {
        return rider.get(Utils.ID);
    }

    public String getRiderRef() {
        return rider.get(Utils.PHOTO_REF);
    }

    public String getRiderName() {
        return rider.get(Utils.NAME);
    }

    public String getOfferorId() {
        return offeror.get(Utils.ID);
    }

    public String getOfferorRef() {
        return offeror.get(Utils.PHOTO_REF);
    }

    public String getOfferorName() {
        return offeror.get(Utils.NAME);
    }

    public ArrayList<Double> getLocation() {
        return location;
    }

    public void setLocation(ArrayList<Double> location) {
        this.location = location;
    }
}
