package com.sunrise.dental.service;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.TreatmentTypeDAO;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.TreatmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests AppointmentManager's orchestration logic in isolation from the
 * database, by mocking the four DAOs it depends on. Uses the
 * dependency-injection constructor added specifically for testing.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentManagerTest {

    @Mock private AppointmentDAO appointmentDAO;
    @Mock private PatientDAO patientDAO;
    @Mock private BillDAO billDAO;
    @Mock private TreatmentTypeDAO treatmentTypeDAO;

    private AppointmentManager manager;

    @BeforeEach
    void setUp() {
        manager = new AppointmentManager(appointmentDAO, patientDAO, billDAO, treatmentTypeDAO);
    }

    // ---------- login() ----------

    @Test
    void loginSucceedsWithCorrectCredentials() {
        assertTrue(manager.login("admin", "admin123"));
    }

    @Test
    void loginFailsWithWrongPassword() {
        assertFalse(manager.login("admin", "wrongPassword"));
    }

    // ---------- registerAppointment() ----------

    @Test
    void registerAppointmentSavesPatientAndAppointmentWhenValid() throws SQLException {
        Appointment result = manager.registerAppointment(
                "Nimal Perera", "123 Galle Road, Colombo", "0771234567",
                "D001", "Scaling",
                LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        // Confirms the manager actually calls save() on both DAOs — this is
        // the orchestration behaviour being tested, not the DB itself.
        verify(patientDAO, times(1)).save(any(Patient.class));
        verify(appointmentDAO, times(1)).save(any(Appointment.class));
        assertNotNull(result);
        assertEquals("Scaling", result.getTreatment());
    }

    @Test
    void registerAppointmentRejectsInvalidInputWithoutTouchingDatabase() {
        // Blank patient name should fail Appointment.validate() before any
        // DAO is ever called.
        assertThrows(IllegalArgumentException.class, () ->
                manager.registerAppointment(
                        "", "123 Galle Road, Colombo", "0771234567",
                        "D001", "Scaling",
                        LocalDate.now().plusDays(1), LocalTime.of(10, 0))
        );

        verifyNoInteractions(patientDAO, appointmentDAO);
    }

    // ---------- cancelAppointment() ----------

    @Test
    void cancelAppointmentReturnsFalseWhenAppointmentNotFound() throws SQLException {
        when(appointmentDAO.findByApptNumber("APT-9999")).thenReturn(null);

        boolean result = manager.cancelAppointment("APT-9999");

        assertFalse(result);
        // Should never attempt to cancel something that was never found.
        verify(appointmentDAO, never()).cancel(anyString());
    }

    @Test
    void cancelAppointmentCancelsWhenFound() throws SQLException {
        Appointment existing = new Appointment();
        existing.setApptNumber("APT-0001");
        when(appointmentDAO.findByApptNumber("APT-0001")).thenReturn(existing);
        when(appointmentDAO.cancel("APT-0001")).thenReturn(true);

        boolean result = manager.cancelAppointment("APT-0001");

        assertTrue(result);
        verify(appointmentDAO, times(1)).cancel("APT-0001");
    }

    // ---------- generateBill() ----------

    @Test
    void generateBillLooksUpTreatmentCostAndSaves() throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setApptNumber("APT-0001");
        appointment.setTreatment("Scaling");
        when(appointmentDAO.findByApptNumber("APT-0001")).thenReturn(appointment);

        TreatmentType scaling = new TreatmentType();
        scaling.setName("Scaling");
        scaling.setPrice(2500.00);
        when(treatmentTypeDAO.findByName("Scaling")).thenReturn(scaling);

        when(billDAO.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bill bill = manager.generateBill("APT-0001", 1000.00);

        assertEquals(2500.00, bill.getTreatmentCost());
        assertEquals(1000.00, bill.getConsultFee());
        assertTrue(bill.getTotalAmount() > 0, "calculateTotal() should have been applied before saving");
        verify(billDAO, times(1)).save(any(Bill.class));
    }

    @Test
    void generateBillThrowsWhenAppointmentNotFound() throws SQLException {
        when(appointmentDAO.findByApptNumber("APT-DOES-NOT-EXIST")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> manager.generateBill("APT-DOES-NOT-EXIST", 1000.00));

        verifyNoInteractions(billDAO);
    }
}