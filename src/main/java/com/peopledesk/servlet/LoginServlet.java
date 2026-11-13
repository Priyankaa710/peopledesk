package com.peopledesk.servlet;

import com.peopledesk.dao.UserDAO;
import com.peopledesk.model.User;
import com.peopledesk.util.CsrfUtil;
import com.peopledesk.util.Flash;
import com.peopledesk.util.HtmlUtil;
import com.peopledesk.util.PageTemplate;
import com.peopledesk.util.PasswordUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Shows the login page (GET) and checks the username and password (POST). URL: /login */
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String DB_DOWN_MESSAGE =
            "The database is not reachable right now. Check that MySQL is running and the configuration is correct.";

    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        StringBuilder notices = new StringBuilder();
        String msg = req.getParameter("msg");
        if ("loggedout".equals(msg)) {
            notices.append(Flash.alert("success", "You have been logged out."));
        } else if ("setupdone".equals(msg)) {
            notices.append(Flash.alert("success", "Administrator account created. You can sign in now."));
        }

        // First run? Offer the one-time setup page.
        try {
            if (userDao.countUsers() == 0) {
                notices.append("<div class=\"alert alert-info\">First time here? "
                        + "<a href=\"" + req.getContextPath() + "/setup\">Create the administrator account</a>.</div>\n");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not check the users table", e);
            notices.append(Flash.alert("error", DB_DOWN_MESSAGE));
        }

        showForm(req, resp, "", notices.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        // 1. Validate empty fields
        if (username.isEmpty() || password.isEmpty()) {
            showForm(req, resp, username, Flash.alert("error", "Please enter both username and password."));
            return;
        }
        if (username.length() > 50 || password.length() > 200) {
            showForm(req, resp, username, Flash.alert("error", "Invalid username or password."));
            return;
        }

        try {
            // 2. Look the user up and compare the password with the stored hash
            User user = userDao.findByUsername(username);
            boolean passwordOk;
            if (user == null) {
                PasswordUtil.spendTime(password); // same delay whether or not the username exists
                passwordOk = false;
            } else {
                passwordOk = PasswordUtil.verify(password, user.getPasswordHash());
            }

            if (!passwordOk) {
                // Same message for "no such user" and "wrong password" - do not help attackers.
                showForm(req, resp, username, Flash.alert("error", "Invalid username or password."));
                return;
            }

            // 3. Success: throw away any old session and start a fresh one (prevents session fixation)
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            CsrfUtil.getToken(session);

            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Login failed because of a database problem", e);
            showForm(req, resp, username, Flash.alert("error", DB_DOWN_MESSAGE));
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, String username, String alertsHtml)
            throws IOException {
        HttpSession session = req.getSession(true); // the session holds the CSRF token
        String ctx = req.getContextPath();

        String body = "<h1 class=\"auth-title\">Sign in</h1>\n"
                + "<p class=\"auth-subtitle\">Enter your account details to continue.</p>\n"
                + alertsHtml
                + "<form method=\"post\" action=\"" + ctx + "/login\" class=\"form\">\n"
                + CsrfUtil.hiddenField(session) + "\n"
                + "  <div class=\"form-group\">\n"
                + "    <label for=\"username\">Username</label>\n"
                + "    <input type=\"text\" id=\"username\" name=\"username\" value=\"" + HtmlUtil.escape(username) + "\""
                + " maxlength=\"50\" autocomplete=\"username\" required autofocus>\n"
                + "  </div>\n"
                + "  <div class=\"form-group\">\n"
                + "    <label for=\"password\">Password</label>\n"
                + "    <input type=\"password\" id=\"password\" name=\"password\""
                + " maxlength=\"200\" autocomplete=\"current-password\" required>\n"
                + "  </div>\n"
                + "  <button type=\"submit\" class=\"btn btn-primary btn-block\">Login</button>\n"
                + "</form>\n";
        PageTemplate.renderPublic(req, resp, "Login", body);
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }
}
