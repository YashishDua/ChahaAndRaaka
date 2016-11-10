package com.example.lenovo.chachaandraaka;

/**
 * Created by Lenovo on 10-11-2016.
 */

public class User {

    public User() {
    }

    public String UserID;
    public String LocationLatitude;
    public String LocationLongitude;
    public String TeamID;

    public User(String userID, String locationLatitude, String locationLongitude, String teamID) {
        UserID = userID;
        LocationLatitude = locationLatitude;
        LocationLongitude = locationLongitude;
        TeamID = teamID;
    }
}


