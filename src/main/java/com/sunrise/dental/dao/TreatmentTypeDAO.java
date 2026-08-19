package com.sunrise.dental.dao;

import com.sunrise.dental.db.DBConnection;
import com.sunrise.dental.model.TreatmentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Read access to the `treatment_type` table. Like DentistDAO, this lets
 * staff add a new treatment type with an INSERT instead of a code change -
 * the Register Appointment dropdown calls TreatmentServlet -> this DAO to
 * populate itself.
 */
public class TreatmentTypeDAO {

    /**
     * Returns every treatment type, ordered by name.
     */
    public List<TreatmentType> findAll() throws SQLException {
        List<TreatmentType> treatments = new ArrayList<>();
        String sql = "SELECT treatment_id, name, price FROM treatment_type ORDER BY name";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                TreatmentType treatment = new TreatmentType();
                treatment.setTreatmentId(rs.getString("treatment_id"));
                treatment.setName(rs.getString("name"));
                treatment.setPrice(rs.getDouble("price"));
                treatments.add(treatment);
            }
        }

        return treatments;
    }

    /**
     * Looks up a treatment type by its exact name. Appointment.treatment is
     * stored as the treatment's name (not its id), so billing uses this to
     * find the treatment's price when a bill is generated. Returns null if
     * no treatment type with that name exists (e.g. it was renamed/removed
     * after the appointment was booked).
     */
    public TreatmentType findByName(String name) throws SQLException {
        String sql = "SELECT treatment_id, name, price FROM treatment_type WHERE name = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TreatmentType treatment = new TreatmentType();
                treatment.setTreatmentId(rs.getString("treatment_id"));
                treatment.setName(rs.getString("name"));
                treatment.setPrice(rs.getDouble("price"));
                return treatment;
            }
        }
    }

    /**
     * Inserts a new treatment type, auto-generating the next treatment_id
     * ("T001", "T002", ...). The generated id is set back onto the
     * passed-in TreatmentType so the caller (servlet) can return it to
     * the client. Same pattern as DentistDAO.save().
     */
    public synchronized TreatmentType save(TreatmentType treatment) throws SQLException {
        String sql = "INSERT INTO treatment_type (treatment_id, name, price) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String treatmentId = generateNextTreatmentId(conn);
            treatment.setTreatmentId(treatmentId);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, treatmentId);
                ps.setString(2, treatment.getName());
                ps.setDouble(3, treatment.getPrice());
                ps.executeUpdate();
            }
        }

        return treatment;
    }

    /**
     * Finds the highest existing treatment_id ("T0007") and returns the
     * next one ("T0008"). Starts at "T001" if the table is empty.
     */
    private String generateNextTreatmentId(Connection conn) throws SQLException {
        String sql = "SELECT treatment_id FROM treatment_type ORDER BY treatment_id DESC LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                return "T001";
            }

            String lastId = rs.getString("treatment_id"); // e.g. "T007"
            int number = Integer.parseInt(lastId.substring(1)) + 1;
            return String.format("T%03d", number);
        }
    }
}