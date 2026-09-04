package com.sunrise.dental.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebFilter("/api/*")
public class AuthFilter extends HttpFilter {

    // Endpoints reachable without being logged in.
    private static final String LOGIN_PATH = "/api/login";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        String path = req.getServletPath();

        if (LOGIN_PATH.equals(path)) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession(false); // don't create one just to check
        boolean loggedIn = session != null && Boolean.TRUE.equals(session.getAttribute("loggedIn"));

        if (!loggedIn) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":false,\"message\":\"Not logged in\"}");
            return;
        }

        chain.doFilter(req, resp);
    }
}
