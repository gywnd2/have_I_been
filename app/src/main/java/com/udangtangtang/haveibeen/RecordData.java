package com.udangtangtang.haveibeen;

public class RecordData {
    private String fileName;
    private String locationName;
    private float rating;
    private String comment;

    public RecordData(String fileNameString, String locationNameString , float ratingScore, String commentString){
        fileName=fileNameString;
        locationName=locationNameString;
        rating=ratingScore;
        comment=commentString;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
