package com.sunrise.dental.model;

public class Patient {

    // contact is the primary key (see schema.sql) — no separate patientId.
    private String name;
    private String address;
    private String contact;

    public Patient() {
    }

    public Patient(String name, String address, String contact) {
        this.name = name;
        this.address = address;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
