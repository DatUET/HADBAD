package com.example.hadad.Model;

public class ChatVideo {
    String sender, roomId, reciver, nameSender;

    public ChatVideo(String sender, String roomId, String reciver, String nameSender) {
        this.sender = sender;
        this.roomId = roomId;
        this.reciver = reciver;
        this.nameSender = nameSender;
    }

    public ChatVideo() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getReciver() {
        return reciver;
    }

    public void setReciver(String reciver) {
        this.reciver = reciver;
    }

    public String getNameSender() {
        return nameSender;
    }

    public void setNameSender(String nameSender) {
        this.nameSender = nameSender;
    }
}
