package com.sunrise.dental.dao;

import com.sunrise.dental.model.Appointment;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO (Data Access Object) Design Pattern.
 *
 * This interface defines WHAT operations are available for Appointment
 * persistence, without saying HOW they're implemented. AppointmentDAOImpl
 * provides the MySQL implementation. If you ever swapped databases, only
 * the implementation would change — nothing else in the app would know
 * or care.
 */
public interface AppointmentDAO {

    void save(Appointment appointment) throws SQLException;

    Appointment findByApptNumber(String apptNumber) throws SQLException;

    List<Appointment> findAll() throws SQLException;

    boolean cancel(String apptNumber) throws SQLException;
}
