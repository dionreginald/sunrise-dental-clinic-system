package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.service.AppointmentManager;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Web service endpoint for use case "Display Appointment Details".
 * GET /api/appointments/search?apptNumber=...
 * Returns the Appointment as JSON, or a "not found" JSON object.
 *
 * This mirrors the "found" / "not found" alt fragment from your
 * Display Appointment Details sequence diagram — implement that branching
 * here.
 */
@WebServlet({"/api/appointments/search", "/api/appointments/all"})
public class SearchAppointmentServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        // /api/appointments/all -> return every appointment as a JSON array
        if (req.getServletPath().endsWith("/all")) {
            try {
                List<Appointment> appointments = manager.listAllAppointments();
                resp.getWriter().write(gson.toJson(appointments));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject error = new JsonObject();
                error.addProperty("message", "Server error: " + e.getMessage());
                resp.getWriter().write(gson.toJson(error));
            }
            return;
        }

        // /api/appointments/search?apptNumber=... -> return one appointment
        String apptNumber = req.getParameter("apptNumber");

        try {
            Appointment appointment = manager.searchAppointment(apptNumber);

            if (appointment == null) {
                JsonObject error = new JsonObject();
                error.addProperty("found", false);
                error.addProperty("message", "Appointment not found");
                resp.getWriter().write(gson.toJson(error));
            } else {
                resp.getWriter().write(gson.toJson(appointment));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("message", "Server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }
}
