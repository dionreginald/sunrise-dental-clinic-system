package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.model.Bill;
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
 * Web service endpoint for use case "Calculate and Print Bill".
 * GET  /api/bill?apptNumber=...&consultFee=...  -> calculates a bill PREVIEW only (nothing is saved)
 * GET  /api/bill                                -> lists every recorded bill
 * POST /api/bill  (apptNumber, consultFee)      -> recalculates and RECORDS the bill
 */
@WebServlet("/api/bill")
public class BillServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final BillDAO billDAO = new BillDAO();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String apptNumber = req.getParameter("apptNumber");
        String consultFeeParam = req.getParameter("consultFee");
        resp.setContentType("application/json");

        // No consultFee supplied -> this is a request for the bill history,
        // not a request to preview a new bill.
        if (consultFeeParam == null) {
            try {
                List<Bill> bills = billDAO.findAll();
                resp.getWriter().write(gson.toJson(bills));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject error = new JsonObject();
                error.addProperty("message", "Server error: " + e.getMessage());
                resp.getWriter().write(gson.toJson(error));
            }
            return;
        }

        // consultFee supplied via GET -> PREVIEW only. Nothing is written to
        // the database here; the user has to hit "Save Bill" (POST, below)
        // to actually record it.
        try {
            double consultFee = Double.parseDouble(consultFeeParam);
            Bill bill = manager.calculateBill(apptNumber, consultFee);
            resp.getWriter().write(gson.toJson(bill));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("message", "consultFee must be a valid number");
            resp.getWriter().write(gson.toJson(error));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("message", e.getMessage());
            resp.getWriter().write(gson.toJson(error));
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

        String apptNumber = req.getParameter("apptNumber");
        String consultFeeParam = req.getParameter("consultFee");
        resp.setContentType("application/json");

        try {
            if (consultFeeParam == null) {
                throw new IllegalArgumentException("consultFee is required");
            }
            double consultFee = Double.parseDouble(consultFeeParam);
            // Recalculates from scratch server-side (rather than trusting a
            // total the client sends back) and records it via BillDAO.save().
            Bill bill = manager.saveBill(apptNumber, consultFee);
            resp.getWriter().write(gson.toJson(bill));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("message", "consultFee must be a valid number");
            resp.getWriter().write(gson.toJson(error));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("message", e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("message", "Server error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        }
    }
}
