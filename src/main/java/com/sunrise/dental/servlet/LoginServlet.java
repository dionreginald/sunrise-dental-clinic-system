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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Web service endpoint for use case "User Authentication (Login)".
 * POST /api/login   body: username=...&password=...
 * Returns JSON: {"success": true/false, "message": "..."}
 *
 * On success this creates an HttpSession and marks it "loggedIn" — that
 * session is what AuthFilter checks on every other /api/* request, so the
 * login is enforced server-side, not just by hiding the login form in the
 * browser.
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final AppointmentManager manager = new AppointmentManager();
    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        JsonObject json = new JsonObject();
        try {
            boolean success = manager.login(username, password);
            json.addProperty("success", success);
            json.addProperty("message", success ? "Login successful" : "Invalid username or password");

            if (success) {
                // Invalidate any stale session first so a previous login
                // (possibly for a different user, on a shared machine)
                // can't linger, then start a fresh authenticated session.
                HttpSession old = req.getSession(false);
                if (old != null) {
                    old.invalidate();
                }
                HttpSession session = req.getSession(true);
                session.setAttribute("loggedIn", Boolean.TRUE);
                session.setAttribute("username", username);
            }
        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", "Server error: " + e.getMessage());
        }

        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(json));
    }
}
