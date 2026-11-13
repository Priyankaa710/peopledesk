package com.peopledesk.filter;

import com.peopledesk.util.CsrfUtil;
import com.peopledesk.util.Flash;
import com.peopledesk.util.PageTemplate;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The "security guard" - runs before EVERY request (see web.xml).
 *
 *  1. Public pages (login, setup, CSS) are allowed for everyone.
 *  2. Every other page needs a logged-in session; otherwise the user is sent to the login page.
 *  3. Every POST request must carry a valid CSRF token.
 *  4. Protected pages are marked "do not cache", so the browser's Back button cannot show
 *     employee data after logout.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // A few safe default security headers
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");

        // Path inside the application, e.g. "/employees/add" (without the context path)
        String path = req.getServletPath();
        if (req.getPathInfo() != null) {
            path = path + req.getPathInfo();
        }

        boolean isStaticFile = path.startsWith("/css/");
        if (!isStaticFile) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
        }

        // Rule 2: protected pages need a logged-in user
        if (!isPublic(path) && !isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Rule 3: every POST needs a valid CSRF token
        if ("POST".equalsIgnoreCase(req.getMethod()) && !CsrfUtil.isValid(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            String body = Flash.alert("error", "Your form has expired or is not valid. Please go back and try again.")
                    + "<p><a class=\"btn btn-primary\" href=\"" + req.getContextPath() + "/login\">Back to PeopleDesk</a></p>";
            PageTemplate.renderPublic(req, resp, "Request rejected", body);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return path.equals("/")
                || path.equals("/index.html")
                || path.equals("/login")
                || path.equals("/setup")
                || path.equals("/error")
                || path.startsWith("/css/");
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false); // false = do not create a new session
        return session != null && session.getAttribute("userId") != null;
    }
}
