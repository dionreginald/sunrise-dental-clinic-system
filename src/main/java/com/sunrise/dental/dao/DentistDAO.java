package com.sunrise.dental.dao;

import com.sunrise.dental.db.DBConnection;
import com.sunrise.dental.model.Dentist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Read access to the `dentist` table. Dentists are seeded/managed directly
 * in the database (see database/schema.sql) rather than hardcoded in the
 * frontend, so adding a new dentist is just an INSERT — no code change and
 * no redeploy needed. The Register Appointment dropdown calls
 * DentistServlet -> this DAO to populate itself.
 */
public class DentistDAO {

    /**
     * Returns every dentist, ordered by name so dropdowns list them
     * alphabetically.
     */
    public List<Dentist> findAll() throws SQLException {
        List<Dentist> dentists = new ArrayList<>();
        String sql = "SELECT dentist_id, name, specialty FROM dentist ORDER BY name";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                dentists.add(mapRow(rs));
            }
        }

        return dentists;
    }

    /**
     * Looks up a single dentist by id. Returns null if no matching dentist
     * exists.
     */
    public Dentist findById(String dentistId) throws SQLException {
        String sql = "SELECT dentist_id, name, specialty FROM dentist WHERE dentist_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dentistId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    private Dentist mapRow(ResultSet rs) throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setDentistId(rs.getString("dentist_id"));
        dentist.setName(rs.getString("name"));
        dentist.setSpecialty(rs.getString("specialty"));
        return dentist;
    }

    /**
     * Inserts a new dentist, auto-generating the next dentist_id ("D001",
     * "D002", ...) the same way AppointmentDAOImpl generates appt_number.
     * The generated id is set back onto the passed-in Dentist so the
     * caller (servlet) can return it to the client.
     */
    public Dentist save(Dentist dentist) throws SQLException {
        String sql = "INSERT INTO dentist (dentist_id, name, specialty) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String dentistId = generateNextDentistId(conn);
            dentist.setDentistId(dentistId);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dentistId);
                ps.setString(2, dentist.getName());
                ps.setString(3, dentist.getSpecialty());
                ps.executeUpdate();
            }
        }

        return dentist;
    }

    /**
     * Finds the highest existing dentist_id ("D0007") and returns the next
     * one ("D0008"). Starts at "D001" if the table is empty. Same pattern
     * as AppointmentDAOImpl.generateNextApptNumber().
     */
    private String generateNextDentistId(Connection conn) throws SQLException {
        String sql = "SELECT dentist_id FROM dentist ORDER BY dentist_id DESC LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                return "D001";
            }

            String lastId = rs.getString("dentist_id"); // e.g. "D002"
            int number = Integer.parseInt(lastId.substring(1)) + 1;
            return String.format("D%03d", number);
        }
    }
}
