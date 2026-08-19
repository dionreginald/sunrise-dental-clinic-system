package com.sunrise.dental.dao;

import com.sunrise.dental.db.DBConnection;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAOImpl implements AppointmentDAO {

    /**
     * Generates the next appointment number ("APT-0001", "APT-0002", ...)
     * and inserts the row. Assumes appointment.getPatient() and
     * getDentist() are already saved/existing (patient by contact,
     * dentist by dentist_id) — this DAO only touches the appointment table.
     */
    @Override
    public synchronized void save(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointment "
                + "(appt_number, patient_contact, dentist_id, treatment, appt_date, appt_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String apptNumber = generateNextApptNumber(conn);
            appointment.setApptNumber(apptNumber);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, apptNumber);
                ps.setString(2, appointment.getPatient().getContact());
                ps.setString(3, appointment.getDentist().getDentistId());
                ps.setString(4, appointment.getTreatment());
                ps.setDate(5, java.sql.Date.valueOf(appointment.getApptDate()));
                ps.setTime(6, java.sql.Time.valueOf(appointment.getApptTime()));
                ps.setString(7, appointment.getStatus());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Looks up one appointment by number, joining across patient and
     * dentist so the returned Appointment has full nested Patient/Dentist
     * objects (not just their IDs). Returns null if not found.
     */
    @Override
    public Appointment findByApptNumber(String apptNumber) throws SQLException {
        String sql = "SELECT a.appt_number, a.treatment, a.appt_date, a.appt_time, a.status, "
                + "p.contact, p.name AS patient_name, p.address, "
                + "d.dentist_id, d.name AS dentist_name, d.specialty "
                + "FROM appointment a "
                + "JOIN patient p ON a.patient_contact = p.contact "
                + "JOIN dentist d ON a.dentist_id = d.dentist_id "
                + "WHERE a.appt_number = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apptNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    /**
     * Returns every appointment, most recently added first isn't guaranteed
     * here (no ORDER BY) — add one if your UI needs a specific order.
     */
    @Override
    public List<Appointment> findAll() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();

        String sql = "SELECT a.appt_number, a.treatment, a.appt_date, a.appt_time, a.status, "
                + "p.contact, p.name AS patient_name, p.address, "
                + "d.dentist_id, d.name AS dentist_name, d.specialty "
                + "FROM appointment a "
                + "JOIN patient p ON a.patient_contact = p.contact "
                + "JOIN dentist d ON a.dentist_id = d.dentist_id";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                appointments.add(mapRow(rs));
            }
        }

        return appointments;
    }

    /**
     * Soft-cancel: sets status = 'CANCELLED' rather than deleting the row,
     * so the appointment history/audit trail is preserved. Returns true if
     * a row was actually updated (i.e. the appt_number existed).
     */
    @Override
    public boolean cancel(String apptNumber) throws SQLException {
        String sql = "UPDATE appointment SET status = 'CANCELLED' WHERE appt_number = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apptNumber);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Builds an Appointment (with nested Patient and Dentist) from the
     * current row of a ResultSet produced by the joined SELECTs above.
     */
    private Appointment mapRow(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setContact(rs.getString("contact"));
        patient.setName(rs.getString("patient_name"));
        patient.setAddress(rs.getString("address"));

        Dentist dentist = new Dentist();
        dentist.setDentistId(rs.getString("dentist_id"));
        dentist.setName(rs.getString("dentist_name"));
        dentist.setSpecialty(rs.getString("specialty"));

        Appointment appointment = new Appointment();
        appointment.setApptNumber(rs.getString("appt_number"));
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(rs.getString("treatment"));
        appointment.setApptDate(rs.getDate("appt_date").toLocalDate());
        appointment.setApptTime(rs.getTime("appt_time").toLocalTime());
        appointment.setStatus(rs.getString("status"));
        return appointment;
    }

    /**
     * Finds the highest existing appt_number ("APT-0007") and returns the
     * next one ("APT-0008"). Starts at "APT-0001" if the table is empty.
     * Same pattern as BillDAO.generateNextBillId().
     */
    private String generateNextApptNumber(Connection conn) throws SQLException {
        String sql = "SELECT appt_number FROM appointment ORDER BY appt_number DESC LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                return "APT-0001";
            }

            String lastNumber = rs.getString("appt_number"); // e.g. "APT-0007"
            int number = Integer.parseInt(lastNumber.substring(4)) + 1;
            return String.format("APT-%04d", number);
        }
    }
}