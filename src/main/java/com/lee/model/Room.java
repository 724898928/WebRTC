package com.lee.model;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private String roomId;
    private String roomUrl;
    private User user;
    private Map<String, User> userMap = new ConcurrentHashMap<>();

    public Room(String id) {
        this.roomId = id;
    }
    public Room(String id, String room_url,User user) {
        this.roomId = id;
        this.roomUrl = room_url;
        if (null!= user){
            this.user = user;
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public void addUser(User user) {
        if (null != user)
            this.userMap.put(user.getUserId(), user);
    }

    public Map<String, User> users() {
        return this.userMap;
    }

    public User getUser() {
        return user;
    }
    public User getUserFromMap(String userId) {
        return this.userMap.get(userId);
    }

    public void setId(String id) {
        this.roomId = id;
    }

    public String getRoomUrl() {
        return roomUrl;
    }

    public void setRoomUrl(String roomUrl) {
        this.roomUrl = roomUrl;
    }

    public void delUser(String userId) {
        User user = this.userMap.remove(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId.equals(room.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
