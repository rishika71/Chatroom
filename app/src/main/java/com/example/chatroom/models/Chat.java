package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Chat implements Serializable {

    public String id;

    public String content, owner, display;

    public Date created_at;

    public ArrayList<String> likedBy;

    public Chat() {
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public void addLike(String uid) {
        this.likedBy.add(uid);
    }

    public void unLike(String uid) {
        this.likedBy.remove(uid);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Date getCreated_at() {
        return (created_at == null ? new Date() : created_at);
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", owner='" + owner + '\'' +
                ", display='" + display + '\'' +
                ", created_at=" + created_at +
                ", likedBy=" + likedBy +
                '}';
    }
}