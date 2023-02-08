package com.lee.model;

import com.google.gson.Gson;

public class User {

    private String userId;
    private String name;
    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId) &&
                name.equals(user.name);
    }

    @Override
    public int hashCode() {
        if (userId == null)
            return 0;
        int result = 1;
        result = userId.hashCode()+ 31 * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
