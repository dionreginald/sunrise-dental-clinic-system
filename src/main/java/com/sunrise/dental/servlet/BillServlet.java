package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.service.AppointmentManager;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Web service endpoint for use case "Calculate and Print Bill".
 * GET /api/bill?apptNumber=...
 */
@WebServlet("/api/bill")
public class BillServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String apptNumber = req.getParameter("apptNumber");
        String consultFeeParam = req.getParameter("consultFee");
        resp.setContentType("application/json");

        try {
            double consultFee = Double.parseDouble(consultFeeParam);
            Bill bill = manager.generateBill(apptNumber, consultFee);
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
