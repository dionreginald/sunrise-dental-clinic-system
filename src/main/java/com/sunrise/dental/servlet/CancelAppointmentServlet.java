package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.service.AppointmentManager;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Web service endpoint for use case "Cancel Appointment".
 * POST /api/appointments/cancel   body: apptNumber=...
 */
@WebServlet("/api/appointments/cancel")
public class CancelAppointmentServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String apptNumber = req.getParameter("apptNumber");
        resp.setContentType("application/json");
        JsonObject json = new JsonObject();

        try {
            boolean cancelled = manager.cancelAppointment(apptNumber);
            json.addProperty("success", cancelled);
            json.addProperty("message", cancelled ? "Appointment cancelled" : "Appointment not found");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("success", false);
            json.addProperty("message", "Server error: " + e.getMessage());
        }

        resp.getWriter().write(gson.toJson(json));
    }
}
