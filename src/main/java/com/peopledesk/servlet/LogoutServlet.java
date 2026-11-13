package com.peopledesk.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Logs the user out. URL: /logout
 * Only POST ends the session (a link click must never log someone out; the form carries a CSRF token).
 */
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // removes everything stored in the session
        }
        resp.sendRedirect(req.getContextPath() + "/login?msg=loggedout");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Opening /logout in the address bar does nothing harmful.
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
