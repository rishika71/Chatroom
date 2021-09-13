package com.example.chatroom.models;

import java.io.Serializable;

public class User implements Serializable {

    String firstname, lastname, photoref, city, email, gender, id;

    Chatroom chatroom;

    Ride ride;

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public User() {
    }

    public User(String firstname, String lastname, String photoref, String city, String email, String gender, String id) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.photoref = photoref;
        this.city = city;
        this.email = email;
        this.gender = gender;
        this.id = id;
    }

    public String getDisplayName() {
        return this.firstname + " " + this.lastname;
    }

    @Override
    public String toString() {
        return firstname + " " + lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhotoref() {
        return photoref;
    }

    public void setPhotoref(String photoref) {
        this.photoref = photoref;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
