package com.sunrise.dental.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sunrise.dental.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/logout — invalidates the current session server-side.
 * Before this existed, "logout" only hid the app section in the browser;
 * the session (and therefore access to every /api/* endpoint) stayed
 * valid until the browser tab closed. This is mapped outside AuthFilter's
 * protected set is not needed — it's fine for it to require a session too,
 * since there's nothing useful to "log out" of if you were never logged in.
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    private final Gson gson = JsonUtil.GSON;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", "Logged out");

        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(json));
    }
}
