package com.example.chatroom.models;

import com.google.android.gms.maps.model.LatLng;

public class Ride {

    public LatLng pickup, drop;

    public String msg_id;

    public Ride() {
    }

    public Ride(LatLng pickup, LatLng drop) {
        this.pickup = pickup;
        this.drop = drop;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "pickup=" + pickup +
                ", drop=" + drop +
                '}';
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
