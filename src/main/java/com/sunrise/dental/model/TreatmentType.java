package com.sunrise.dental.model;

/**
 * A selectable treatment type (e.g. "Scaling & Polishing"). Stored in the
 * `treatment_type` table so the list is managed data, not a hardcoded
 * dropdown in the frontend — matches the Dentist model's pattern.
 */
public class TreatmentType {

    private String treatmentId;
    private String name;
    private double price;

    public TreatmentType() {
    }

    public TreatmentType(String treatmentId, String name, double price) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.price = price;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
