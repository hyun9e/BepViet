package com.example.bepviet02.models;

public class User {
    String id;
    String name;
    String email;
    String avatar;
    String cover;

    public User() {
    }

    public User(String id, String name, String email, String image, String cover) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = image;
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String image) {
        this.avatar = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
