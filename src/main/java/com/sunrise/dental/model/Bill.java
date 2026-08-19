package com.sunrise.dental.model;

public class Bill {

    // Fixed charges — change these two constants if your brief specifies
    // different figures; every bill calculation reads from here.
    private static final double HOSPITAL_CHARGE = 500.00;
    private static final double TAX_PERCENTAGE = 8.0;

    private String billId;
    private Appointment appointment;
    private double consultFee;
    private double treatmentCost;
    private double hospitalCharge = HOSPITAL_CHARGE;
    private double taxPercentage = TAX_PERCENTAGE;
    private double totalAmount;

    public Bill() {
    }

    public Bill(Appointment appointment, double consultFee) {
        this.appointment = appointment;
        this.consultFee = consultFee;
    }

    public Bill(Appointment appointment, double consultFee, double treatmentCost) {
        this.appointment = appointment;
        this.consultFee = consultFee;
        this.treatmentCost = treatmentCost;
    }

    /**
     * total = (consultation fee + treatment cost, looked up from the
     * appointment's treatment type + fixed hospital charge), plus tax on
     * top of that subtotal. Sets totalAmount and returns it, so callers can
     * either use the return value or read getTotalAmount() afterwards.
     */
    public double calculateTotal() {
        double subtotal = consultFee + treatmentCost + hospitalCharge;
        double tax = subtotal * (taxPercentage / 100.0);
        totalAmount = subtotal + tax;
        return totalAmount;
    }

    public void printBill() {
        System.out.println("----- Sunrise Dental Clinic -----");
        System.out.println("Bill ID: " + billId);
        System.out.println(appointment != null ? appointment.getDetails() : "No appointment linked");
        System.out.println("Consultation Fee: " + consultFee);
        System.out.println("Treatment Cost: " + treatmentCost);
        System.out.println("Hospital Charge: " + hospitalCharge);
        System.out.println("Tax (" + taxPercentage + "%): " + (totalAmount - consultFee - treatmentCost - hospitalCharge));
        System.out.println("Total Amount: " + totalAmount);
        System.out.println("----------------------------------");
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public double getConsultFee() {
        return consultFee;
    }

    public void setConsultFee(double consultFee) {
        this.consultFee = consultFee;
    }

    public double getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(double treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public double getHospitalCharge() {
        return hospitalCharge;
    }

    public void setHospitalCharge(double hospitalCharge) {
        this.hospitalCharge = hospitalCharge;
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(double taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}