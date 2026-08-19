package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.dao.TreatmentTypeDAO;
import com.sunrise.dental.model.TreatmentType;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Web service endpoint for treatment types, backed by the `treatment_type`
 * table.
 * GET  /api/treatments  - list all treatment types (used by the Register
 *                          Appointment treatment dropdown).
 * POST /api/treatments  - add a new treatment type. body: name
 *                          (used by the "Manage Dentists & Treatments" tab).
 */
@WebServlet("/api/treatments")
public class TreatmentServlet extends HttpServlet {

    private final TreatmentTypeDAO treatmentDAO = new TreatmentTypeDAO();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            resp.getWriter().write(gson.toJson(treatmentDAO.findAll()));
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
            String priceParam = req.getParameter("price");

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Treatment name is required");
            }

            double price = 0.0;
            if (priceParam != null && !priceParam.trim().isEmpty()) {
                try {
                    price = Double.parseDouble(priceParam.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Price must be a valid number");
                }
                if (price < 0) {
                    throw new IllegalArgumentException("Price cannot be negative");
                }
            }

            TreatmentType treatment = new TreatmentType();
            treatment.setName(name.trim());
            treatment.setPrice(price);

            TreatmentType saved = treatmentDAO.save(treatment);

            json.addProperty("success", true);
            json.addProperty("treatmentId", saved.getTreatmentId());
            json.addProperty("message", "Treatment type added successfully");
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