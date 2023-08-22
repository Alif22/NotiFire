package com.example.notifire;

public class OfficerModal {
    private int officerID;
    private String officerName;
    private String email;
    private String password;
    private String department;
    private String availibility;

    public OfficerModal() {
    }

    public OfficerModal(int officerID, String officerName, String department, String availibility) {
        this.officerID = officerID;
        this.officerName = officerName;
        this.department = department;
        this.availibility = availibility;
    }

    public int getOfficerID() {
        return officerID;
    }

    public void setOfficerID(int officerID) {
        this.officerID = officerID;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAvailibility() {
        return availibility;
    }

    public void setAvailibility(String availibility) {
        this.availibility = availibility;
    }
}
