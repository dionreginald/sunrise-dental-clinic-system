package com.sunrise.dental.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Appointment.validate() — covers every rule listed in the
 * method's own Javadoc: blank patient fields, contact format, past dates,
 * and clinic operating hours.
 */
class AppointmentTest {

    // Helper: builds a fully valid appointment so each test only needs to
    // break ONE field, keeping every test focused on a single rule.
    private Appointment buildValidAppointment() {
        Patient patient = new Patient("Nimal Perera", "123 Galle Road, Colombo", "0771234567");
        Dentist dentist = new Dentist("D001", "Dr. Silva", "General Dentistry");
        return new Appointment(
                patient,
                dentist,
                "Scaling",
                LocalDate.now().plusDays(1),   // tomorrow — safely in the future
                LocalTime.of(10, 0)             // 10:00 AM — within clinic hours
        );
    }

    @Test
    void validAppointmentPasses() {
        Appointment appt = buildValidAppointment();

        boolean result = appt.validate();

        assertTrue(result, "A fully valid appointment should pass validation");
        assertTrue(appt.getValidationErrors().isEmpty(), "No errors expected for a valid appointment");
    }

    @Test
    void blankPatientNameFails() {
        Appointment appt = buildValidAppointment();
        appt.getPatient().setName("");   // break just this one field

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Patient name cannot be blank."));
    }

    @Test
    void blankPatientAddressFails() {
        Appointment appt = buildValidAppointment();
        appt.getPatient().setAddress("   ");   // whitespace-only counts as blank

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Patient address cannot be blank."));
    }

    @Test
    void invalidContactFormatFails() {
        Appointment appt = buildValidAppointment();
        appt.getPatient().setContact("12345");   // only 5 digits, needs exactly 10

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Patient contact must be exactly 10 digits."));
    }

    @Test
    void pastDateFails() {
        Appointment appt = buildValidAppointment();
        appt.setApptDate(LocalDate.now().minusDays(1));   // yesterday

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Appointment date cannot be in the past."));
    }

    @Test
    void timeBeforeClinicOpensFails() {
        Appointment appt = buildValidAppointment();
        appt.setApptTime(LocalTime.of(8, 0));   // clinic opens at 9:00 AM

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Appointment time must be between 9:00 AM and 5:00 PM."));
    }

    @Test
    void timeAfterClinicClosesFails() {
        Appointment appt = buildValidAppointment();
        appt.setApptTime(LocalTime.of(18, 0));   // clinic closes at 5:00 PM

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Appointment time must be between 9:00 AM and 5:00 PM."));
    }

    @Test
    void nullPatientFailsWithClearMessage() {
        Appointment appt = buildValidAppointment();
        appt.setPatient(null);   // simulate a caller forgetting to attach patient details

        boolean result = appt.validate();

        assertFalse(result);
        assertTrue(appt.getValidationErrors().contains("Patient details are required."));
    }

    @Test
    void multipleFailuresAreAllReported() {
        // Confirms validate() collects EVERY error, not just the first one —
        // useful to know for how you display errors in the UI.
        Appointment appt = buildValidAppointment();
        appt.getPatient().setName("");
        appt.setApptDate(LocalDate.now().minusDays(1));

        boolean result = appt.validate();

        assertFalse(result);
        assertEquals(2, appt.getValidationErrors().size(),
                "Both the blank-name error and the past-date error should be reported together");
    }
}