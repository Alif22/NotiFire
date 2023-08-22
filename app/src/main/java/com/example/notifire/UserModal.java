package com.example.notifire;

public class UserModal {
    private String Name;
    private Integer AddressID;
    private String PhoneNumber;
    private String IDNumber;
    private String Password;
    private String Type;
    private String Email;
    private Integer Id;

    public UserModal() {
    }

    public UserModal(String name, Integer addressID, String phoneNumber, String IDNumber, String password, String type, String email, Integer id) {
        Name = name;
        AddressID = addressID;
        PhoneNumber = phoneNumber;
        this.IDNumber = IDNumber;
        Password = password;
        Type = type;
        Email = email;
        Id = id;
    }
    public UserModal( Integer id, String name, String IDNumber) {
        Name = name;
        this.IDNumber = IDNumber;
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Integer getAddressID() {
        return AddressID;
    }

    public void setAddressID(Integer addressID) {
        AddressID = addressID;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getIDNumber() {
        return IDNumber;
    }

    public void setIDNumber(String IDNumber) {
        this.IDNumber = IDNumber;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }
}
