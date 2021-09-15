package com.example.chatroom.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class RideReq implements Serializable {

    public LatLng pickup, drop;

    public String ride_id;
    public ArrayList<String> requester;
    public ArrayList<String> offer_msgs;

    public RideReq(LatLng pickup, LatLng drop, ArrayList<String> requester, ArrayList<String> offer_msgs) {
        this.pickup = pickup;
        this.drop = drop;
        this.requester = requester;
        this.offer_msgs = offer_msgs;
    }

    public RideReq() {
    }

    public ArrayList<String> getRequester() {
        return requester;
    }

    public void setRequester(ArrayList<String> requester) {
        this.requester = requester;
    }

    public String getRequesterId() {
        return requester.get(Utils.ID);
    }

    public String getRequesterRef() {
        return requester.get(Utils.PHOTO_REF);
    }

    public String getRequesterName() {
        return requester.get(Utils.NAME);
    }

    public ArrayList<String> getOffer_msgs() {
        return offer_msgs;
    }

    public void setOffer_msgs(ArrayList<String> offer_msgs) {
        this.offer_msgs = offer_msgs;
    }

    public void addOfferMsg(String uid) {
        this.offer_msgs.add(uid);
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public LatLng getPickup() {
        return pickup;
    }

    public void setPickup(LatLng pickup) {
        this.pickup = pickup;
    }

    public LatLng getDrop() {
        return drop;
    }

    public void setDrop(LatLng drop) {
        this.drop = drop;
    }
}
