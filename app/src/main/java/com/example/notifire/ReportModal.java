package com.example.notifire;

public class ReportModal {
    private int ID;
    private String comment;
    private String mediaAttachment;
    private String status;
    private int userID;
    private int categoryID;
    private int addressID;
    private String dateAndTime;

    public ReportModal(int ID, String comment, String mediaAttachment, String status, int userID, int categoryID, int addressID, String dateAndTime) {
        this.ID = ID;
        this.comment = comment;
        this.mediaAttachment = mediaAttachment;
        this.status = status;
        this.userID = userID;
        this.categoryID = categoryID;
        this.addressID = addressID;
        this.dateAndTime = dateAndTime;
    }
    public ReportModal(){}

    public ReportModal(int id, int addressid, int categoryid, String comment, String status, String timeAndDate) {
        this.ID = id;
        this.comment = comment;
        this.status = status;
        this.categoryID = categoryid;
        this.addressID = addressid;
        this.dateAndTime = timeAndDate;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMediaAttachment() {
        return mediaAttachment;
    }

    public void setMediaAttachment(String mediaAttachment) {
        this.mediaAttachment = mediaAttachment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public int getAddressID() {
        return addressID;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }
}
