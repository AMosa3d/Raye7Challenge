package com.example.abdel.raye7challenge;

import io.realm.RealmObject;

/**
 * Created by abdel on 6/11/2017.
 */

public class Trip extends RealmObject {
    String Location;
    String Date;
    String Time;

    public Trip() {
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
