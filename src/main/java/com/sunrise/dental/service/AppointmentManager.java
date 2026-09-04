package com.sunrise.dental.service;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.AppointmentDAOImpl;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.TreatmentTypeDAO;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.TreatmentType;

import java.sql.SQLException;
import java.util.List;

/**
 * This is the "business logic" tier — it sits between the Servlets
 * (presentation tier) and the DAOs (data access tier), matching the
 * three-tier architecture described in the assessment brief.
 *
 * It owns the DAOs it needs (composition) and coordinates them to fulfil
 * each use case from the use case diagram.
 */
public class AppointmentManager {

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final BillDAO billDAO;
    private final TreatmentTypeDAO treatmentTypeDAO;

    public AppointmentManager() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.patientDAO = new PatientDAO();
        this.billDAO = new BillDAO();
        this.treatmentTypeDAO = new TreatmentTypeDAO();
    }

    /**
     * Dependency-injection constructor (Task C): lets tests pass in mock
     * DAOs instead of real ones, so AppointmentManager's orchestration
     * logic can be tested without touching a real database. The
     * no-argument constructor above still exists for normal servlet use.
     */
    public AppointmentManager(AppointmentDAO appointmentDAO, PatientDAO patientDAO,
                              BillDAO billDAO, TreatmentTypeDAO treatmentTypeDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.billDAO = billDAO;
        this.treatmentTypeDAO = treatmentTypeDAO;
    }

    // Hardcoded staff credentials — functional for the deadline, but a real
    // system would check a "staff" table with hashed passwords (e.g.
    // BCrypt) instead of a plaintext constant. Worth flagging as a known
    // limitation / "future work" in your report.
    private static final String STAFF_USERNAME = "admin";
    private static final String STAFF_PASSWORD = "admin123";

    public boolean login(String username, String password) {
        return STAFF_USERNAME.equals(username) && STAFF_PASSWORD.equals(password);
    }

    /**
     * Orchestrates use case "Register New Appointment":
     *  1. build Patient + Dentist + Appointment objects from the input
     *  2. validate the appointment
     *  3. if valid: upsert the patient (new or returning, keyed on contact),
     *     then save the appointment (which generates its apptNumber)
     *  4. return the saved Appointment
     * Throws IllegalArgumentException with the validation messages if the
     * input fails validate() — the servlet should catch this and show the
     * message(s) back to the user.
     */
    public Appointment registerAppointment(String patientName, String patientAddress, String patientContact,
                                           String dentistId, String treatment,
                                           java.time.LocalDate apptDate, java.time.LocalTime apptTime) throws SQLException {

        Patient patient = new Patient(patientName, patientAddress, patientContact);

        Dentist dentist = new Dentist();
        dentist.setDentistId(dentistId);

        Appointment appointment = new Appointment(patient, dentist, treatment, apptDate, apptTime);

        if (!appointment.validate()) {
            throw new IllegalArgumentException(String.join("; ", appointment.getValidationErrors()));
        }

        patientDAO.save(patient); // upsert: inserts new patients, updates returning ones
        appointmentDAO.save(appointment); // generates and sets appointment.apptNumber

        return appointment;
    }

    /**
     * Use case "Display Appointment Details" — mostly a direct pass-through
     * to the DAO, which is why it's already wired up for you.
     */
    public Appointment searchAppointment(String apptNumber) throws SQLException {
        return appointmentDAO.findByApptNumber(apptNumber);
    }

    public List<Appointment> listAllAppointments() throws SQLException {
        return appointmentDAO.findAll();
    }

    /**
     * Orchestrates use case "Cancel Appointment": looks the appointment up
     * first (matches the <<include>> relationship to "Display Appointment
     * Details" in your use case diagram), confirms it exists, then cancels
     * it. Returns false (rather than throwing) if the appt_number doesn't
     * exist, so the servlet can show a simple "not found" message.
     */
    public boolean cancelAppointment(String apptNumber) throws SQLException {
        Appointment existing = searchAppointment(apptNumber);
        if (existing == null) {
            return false;
        }
        return appointmentDAO.cancel(apptNumber);
    }

    /**
     * Orchestrates use case "Calculate Bill" (preview only — nothing is
     * written to the database):
     *  1. searchAppointment(apptNumber) (reuse, matches your <<include>>)
     *  2. build a Bill from it and calculate the total
     *  3. return it, unsaved, so the UI can show a preview before the
     *     user commits to recording it
     * consultFee is passed in since it's set by staff on the day (it can
     * vary by dentist/visit) rather than being fixed data — the caller
     * (servlet) takes it from the billing form. The treatment cost, by
     * contrast, is looked up from the treatment_type table by name so the
     * total actually reflects which treatment was booked, per the brief's
     * "based on treatment type and consultation fee" requirement.
     */
    public Bill calculateBill(String apptNumber, double consultFee) throws SQLException {
        Appointment appointment = searchAppointment(apptNumber);
        if (appointment == null) {
            throw new IllegalArgumentException("No appointment found with number " + apptNumber);
        }

        TreatmentType treatmentType = treatmentTypeDAO.findByName(appointment.getTreatment());
        double treatmentCost = treatmentType != null ? treatmentType.getPrice() : 0.0;

        Bill bill = new Bill(appointment, consultFee, treatmentCost);
        bill.calculateTotal();

        return bill;
    }

    /**
     * Orchestrates use case "Save Bill": re-runs calculateBill() from
     * scratch (rather than trusting numbers a client might send back) and
     * persists the freshly-computed result via BillDAO. Kept separate from
     * calculateBill() so "preview a bill" and "record a bill" are two
     * distinct, explicit actions instead of one action that always writes
     * to the database.
     */
    public Bill saveBill(String apptNumber, double consultFee) throws SQLException {
        Bill bill = calculateBill(apptNumber, consultFee);
        billDAO.save(bill);
        return bill;
    }
}