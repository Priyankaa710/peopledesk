package com.peopledesk.servlet;

import com.peopledesk.dao.UserDAO;
import com.peopledesk.util.CsrfUtil;
import com.peopledesk.util.Flash;
import com.peopledesk.util.HtmlUtil;
import com.peopledesk.util.PageTemplate;
import com.peopledesk.util.PasswordUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * One-time setup page: creates the FIRST administrator account. URL: /setup
 *
 * It only works while the users table is empty. As soon as one user exists, this page
 * redirects to the login page forever, so nobody can use it to create extra admins.
 * The password is hashed before it is stored; no password exists in any file.
 */
public class SetupServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SetupServlet.class.getName());
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,50}$");

    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (userDao.countUsers() > 0) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Setup: could not check the users table", e);
            PageTemplate.renderPublic(req, resp, "Setup",
                    Flash.alert("error", "The database is not reachable right now. Check that MySQL is running "
                            + "and the configuration is correct.")
                    + "<p><a class=\"btn btn-secondary\" href=\"" + req.getContextPath() + "/login\">Back to login</a></p>");
            return;
        }
        showForm(req, resp, "", "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirmPassword") == null ? "" : req.getParameter("confirmPassword");

        if (!USERNAME.matcher(username).matches()) {
            showForm(req, resp, username,
                    Flash.alert("error", "Username must be 3 to 50 characters: letters, digits, dot, dash or underscore."));
            return;
        }
        if (password.length() < 8 || password.length() > 100) {
            showForm(req, resp, username, Flash.alert("error", "Password must be between 8 and 100 characters."));
            return;
        }
        if (!password.equals(confirm)) {
            showForm(req, resp, username, Flash.alert("error", "The two passwords do not match."));
            return;
        }

        try {
            boolean created = userDao.createFirstAdmin(username, PasswordUtil.hash(password));
            if (!created) {
                // An administrator already exists - setup is finished.
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=setupdone");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Setup: could not create the administrator", e);
            showForm(req, resp, username, Flash.alert("error",
                    "The administrator account could not be created. Please check the database and try again."));
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, String username, String alertsHtml)
            throws IOException {
        HttpSession session = req.getSession(true);
        String body = "<h1 class=\"auth-title\">Create administrator</h1>\n"
                + "<p class=\"auth-subtitle\">One-time setup. This page stops working after the first account exists.</p>\n"
                + alertsHtml
                + "<form method=\"post\" action=\"" + req.getContextPath() + "/setup\" class=\"form\">\n"
                + CsrfUtil.hiddenField(session) + "\n"
                + "  <div class=\"form-group\">\n"
                + "    <label for=\"username\">Username</label>\n"
                + "    <input type=\"text\" id=\"username\" name=\"username\" value=\"" + HtmlUtil.escape(username) + "\""
                + " maxlength=\"50\" autocomplete=\"username\" required autofocus>\n"
                + "  </div>\n"
                + "  <div class=\"form-group\">\n"
                + "    <label for=\"password\">Password (at least 8 characters)</label>\n"
                + "    <input type=\"password\" id=\"password\" name=\"password\" minlength=\"8\" maxlength=\"100\""
                + " autocomplete=\"new-password\" required>\n"
                + "  </div>\n"
                + "  <div class=\"form-group\">\n"
                + "    <label for=\"confirmPassword\">Confirm password</label>\n"
                + "    <input type=\"password\" id=\"confirmPassword\" name=\"confirmPassword\" minlength=\"8\""
                + " maxlength=\"100\" autocomplete=\"new-password\" required>\n"
                + "  </div>\n"
                + "  <button type=\"submit\" class=\"btn btn-primary btn-block\">Create administrator</button>\n"
                + "</form>\n";
        PageTemplate.renderPublic(req, resp, "Setup", body);
    }
}
