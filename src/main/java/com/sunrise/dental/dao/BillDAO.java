package com.sunrise.dental.dao;

import com.sunrise.dental.db.DBConnection;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BillDAO {

    /**
     * Inserts the bill (calculating the total first if it hasn't been set)
     * and generates a bill ID in the form "B001", "B002", ... by looking at
     * the highest existing ID and incrementing it.
     */
    public synchronized Bill save(Bill bill) throws SQLException {
        if (bill.getTotalAmount() <= 0) {
            bill.calculateTotal();
        }

        String sql = "INSERT INTO bill (bill_id, appt_number, consult_fee, treatment_cost, hospital_charge, tax_percentage, total_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String billId = generateNextBillId(conn);
            bill.setBillId(billId);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, billId);
                ps.setString(2, bill.getAppointment().getApptNumber());
                ps.setDouble(3, bill.getConsultFee());
                ps.setDouble(4, bill.getTreatmentCost());
                ps.setDouble(5, bill.getHospitalCharge());
                ps.setDouble(6, bill.getTaxPercentage());
                ps.setDouble(7, bill.getTotalAmount());
                ps.executeUpdate();
            }
        }

        return bill;
    }

    /**
     * Looks up the bill for a given appointment number. Returns null if no
     * bill has been raised for that appointment yet. Note: this only
     * populates appointment.apptNumber on the returned Bill (not the full
     * Appointment/Patient/Dentist details) — call AppointmentDAOImpl's
     * findByApptNumber() separately if you need those too.
     */
    public Bill findByApptNumber(String apptNumber) throws SQLException {
        String sql = "SELECT bill_id, appt_number, consult_fee, treatment_cost, hospital_charge, tax_percentage, total_amount "
                + "FROM bill WHERE appt_number = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apptNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Bill bill = new Bill();
                bill.setBillId(rs.getString("bill_id"));

                Appointment appt = new Appointment();
                appt.setApptNumber(rs.getString("appt_number"));
                bill.setAppointment(appt);

                bill.setConsultFee(rs.getDouble("consult_fee"));
                bill.setTreatmentCost(rs.getDouble("treatment_cost"));
                bill.setHospitalCharge(rs.getDouble("hospital_charge"));
                bill.setTaxPercentage(rs.getDouble("tax_percentage"));
                bill.setTotalAmount(rs.getDouble("total_amount"));
                return bill;
            }
        }
    }

    /**
     * Finds the highest existing bill_id (e.g. "B007") and returns the next
     * one ("B008"). Starts at "B001" if the table is empty.
     */
    private String generateNextBillId(Connection conn) throws SQLException {
        String sql = "SELECT bill_id FROM bill ORDER BY bill_id DESC LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                return "B001";
            }

            String lastId = rs.getString("bill_id"); // e.g. "B007"
            int number = Integer.parseInt(lastId.substring(1)) + 1;
            return String.format("B%03d", number);
        }
    }
}