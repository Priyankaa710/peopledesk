package com.peopledesk.servlet;

import com.peopledesk.util.Flash;
import com.peopledesk.util.PageTemplate;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Friendly error page (see <error-page> in web.xml). URL: /error
 * Users see a short message; the technical details go only to the server log.
 */
public class ErrorServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ErrorServlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object statusAttr = req.getAttribute("javax.servlet.error.status_code");
        int status = statusAttr instanceof Integer ? (Integer) statusAttr : 500;

        Object exception = req.getAttribute("javax.servlet.error.exception");
        if (exception instanceof Throwable) {
            LOG.log(Level.SEVERE, "Unhandled error", (Throwable) exception);
        }

        String title;
        String message;
        if (status == 404) {
            title = "Page not found";
            message = "The page you are looking for does not exist.";
        } else if (status == 405) {
            title = "Action not allowed";
            message = "That action is not allowed on this page.";
        } else {
            title = "Something went wrong";
            message = "An unexpected error occurred. Please try again. If it keeps happening, contact the administrator.";
        }

        String body = "<h1 class=\"auth-title\">" + title + "</h1>\n"
                + Flash.alert("error", message)
                + "<p><a class=\"btn btn-primary\" href=\"" + req.getContextPath() + "/dashboard\">Go to PeopleDesk</a></p>\n";
        PageTemplate.renderPublic(req, resp, title, body);
    }
}
