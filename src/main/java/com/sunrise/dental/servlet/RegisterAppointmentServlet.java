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
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Web service endpoint for use case "Register New Appointment".
 * POST /api/appointments   body: name, address, contact, dentistId,
 *                                treatment, apptDate, apptTime
 * apptDate is expected as "yyyy-MM-dd", apptTime as "HH:mm" (both are the
 * default formats an HTML <input type="date"> / <input type="time"> send).
 */
@WebServlet("/api/appointments")
public class RegisterAppointmentServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        JsonObject json = new JsonObject();

        try {
            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String contact = req.getParameter("contact");
            String dentistId = req.getParameter("dentistId");
            String treatment = req.getParameter("treatment");
            LocalDate apptDate = LocalDate.parse(req.getParameter("apptDate"));
            LocalTime apptTime = LocalTime.parse(req.getParameter("apptTime"));

            Appointment appointment = manager.registerAppointment(
                    name, address, contact, dentistId, treatment, apptDate, apptTime);

            json.addProperty("success", true);
            json.addProperty("apptNumber", appointment.getApptNumber());
            json.addProperty("message", "Appointment registered successfully");
            resp.getWriter().write(gson.toJson(json));

        } catch (IllegalArgumentException e) {
            // Thrown by AppointmentManager when appointment.validate() fails,
            // or by LocalDate/LocalTime.parse() on a badly formatted date/time.
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
            resp.getWriter().write(gson.toJson(json));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("success", false);
            json.addProperty("message", "Server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(json));
        }
    }
}
