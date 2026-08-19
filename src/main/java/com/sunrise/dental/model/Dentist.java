package com.sunrise.dental.model;

public class Dentist {

    private String dentistId;
    private String name;
    private String specialty;

    public Dentist() {
    }

    public Dentist(String dentistId, String name, String specialty) {
        this.dentistId = dentistId;
        this.name = name;
        this.specialty = specialty;
    }

    public String getDentistId() {
        return dentistId;
    }

    public void setDentistId(String dentistId) {
        this.dentistId = dentistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
