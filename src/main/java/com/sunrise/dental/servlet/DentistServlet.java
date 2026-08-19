package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.dao.DentistDAO;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Web service endpoint for dentists, backed by the `dentist` table.
 * GET  /api/dentists  - list all dentists (used by the Register
 *                        Appointment dentist dropdown).
 * POST /api/dentists  - add a new dentist. body: name, specialty
 *                        (used by the "Manage Dentists & Treatments" tab).
 */
@WebServlet("/api/dentists")
public class DentistServlet extends HttpServlet {

    private final DentistDAO dentistDAO = new DentistDAO();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            resp.getWriter().write(gson.toJson(dentistDAO.findAll()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("message", "Server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        JsonObject json = new JsonObject();

        try {
            String name = req.getParameter("name");
            String specialty = req.getParameter("specialty");

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Dentist name is required");
            }

            Dentist dentist = new Dentist();
            dentist.setName(name.trim());
            dentist.setSpecialty(specialty == null ? null : specialty.trim());

            Dentist saved = dentistDAO.save(dentist);

            json.addProperty("success", true);
            json.addProperty("dentistId", saved.getDentistId());
            json.addProperty("message", "Dentist added successfully");
            resp.getWriter().write(gson.toJson(json));

        } catch (IllegalArgumentException e) {
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
