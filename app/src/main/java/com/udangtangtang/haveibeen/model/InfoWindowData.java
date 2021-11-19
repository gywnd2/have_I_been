package com.udangtangtang.haveibeen.model;

import android.widget.RatingBar;
import android.widget.TextView;

import com.udangtangtang.haveibeen.R;

public class InfoWindowData {
    private String locationName;
    private String address;
    private String datetime;
    private String comment;
    private float rating;

    public InfoWindowData(){
        this.locationName=null;
        this.datetime=null;
        this.comment=null;
        this.rating=(float)0.0;
    }

    public InfoWindowData(String locationName, String datetime, String comment, String address,float rating) {
        this.locationName = locationName;
        this.datetime = datetime;
        this.comment = comment;
        this.rating = rating;
        this.address=address;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address=address;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
