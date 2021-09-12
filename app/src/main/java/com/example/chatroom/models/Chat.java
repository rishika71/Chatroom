package com.example.chatroom.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Chat implements Serializable {

    public static final int CHAT_MESSAGE = 1;
    public static final int CHAT_LOCATION = 2;
    public static final int CHAT_REQUEST = 3;

    public String id;

    public String content, owner, ownerId, ownerRef;

    public int chatType;

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public void setOwnerRef(String ownerRef) {
        this.ownerRef = ownerRef;
    }

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
                ", created_at=" + created_at +
                ", likedBy=" + likedBy +
                '}';
    }
}
