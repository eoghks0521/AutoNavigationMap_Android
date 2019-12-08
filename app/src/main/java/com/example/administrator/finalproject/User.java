package com.example.administrator.finalproject;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String email, loc;

    public User(){

    }

    public User(String uid, String loc) {
        this.email = uid;
        this.loc = loc;
    }

    public String getEmail() {
        return email;
    }

    public String getloc() {
        return loc;
    }

}