package com.peopledesk.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Builds the common HTML around every page (head, sidebar, top bar, footer), so each servlet
 * only has to create the HTML for its own content. All links start with the context path
 * (for example /PeopleDesk) so the application works under any deployment URL.
 */
public final class PageTemplate {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private PageTemplate() {
    }

    /** A page for people who are not logged in (login, setup, error). */
    public static void renderPublic(HttpServletRequest request, HttpServletResponse response,
                                    String title, String bodyHtml) throws IOException {
        String ctx = request.getContextPath();
        PrintWriter out = openWriter(response);
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println(head(ctx, title));
        out.println("<body class=\"auth-page\">");
        out.println("<div class=\"auth-card\">");
        out.println("  <div class=\"auth-brand\">");
        out.println("    <span class=\"brand-logo\">PD</span>");
        out.println("    <div><div class=\"brand-name\">PeopleDesk</div>");
        out.println("    <div class=\"brand-tagline\">Employee Management System</div></div>");
        out.println("  </div>");
        out.println(bodyHtml);
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * A page for logged-in users: sidebar + top bar + content.
     *
     * @param activeNav which sidebar link to highlight: "dashboard", "employees" or "add"
     */
    public static void renderApp(HttpServletRequest request, HttpServletResponse response,
                                 String title, String activeNav, String bodyHtml) throws IOException {
        String ctx = request.getContextPath();
        HttpSession session = request.getSession(false);
        String username = session == null ? "" : String.valueOf(session.getAttribute("username"));
        String role = session == null ? "" : String.valueOf(session.getAttribute("role"));
        String flash = Flash.consumeHtml(session);
        String csrfField = session == null ? "" : CsrfUtil.hiddenField(session);

        PrintWriter out = openWriter(response);
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println(head(ctx, title));
        out.println("<body>");
        out.println("<div class=\"layout\">");

        // ----- Sidebar -----
        out.println("  <aside class=\"sidebar\">");
        out.println("    <div class=\"sidebar-brand\">");
        out.println("      <span class=\"brand-logo\">PD</span>");
        out.println("      <span class=\"brand-name\">PeopleDesk</span>");
        out.println("    </div>");
        out.println("    <nav class=\"sidebar-nav\">");
        out.println("      " + navLink(ctx + "/dashboard", "Dashboard", "dashboard".equals(activeNav)));
        out.println("      " + navLink(ctx + "/employees", "Employees", "employees".equals(activeNav)));
        out.println("      " + navLink(ctx + "/employees/add", "Add Employee", "add".equals(activeNav)));
        out.println("    </nav>");
        out.println("  </aside>");

        // ----- Main area -----
        out.println("  <div class=\"main\">");
        out.println("    <header class=\"topbar\">");
        out.println("      <div class=\"topbar-user\">Signed in as <strong>" + HtmlUtil.escape(username)
                + "</strong> <span class=\"badge\">" + HtmlUtil.escape(role) + "</span></div>");
        out.println("      <form method=\"post\" action=\"" + ctx + "/logout\" class=\"inline-form\">");
        out.println("        " + csrfField);
        out.println("        <button type=\"submit\" class=\"btn btn-secondary\">Logout</button>");
        out.println("      </form>");
        out.println("    </header>");
        out.println("    <main class=\"content\">");
        out.println("      <h1 class=\"page-title\">" + HtmlUtil.escape(title) + "</h1>");
        out.println(flash);
        out.println(bodyHtml);
        out.println("    </main>");
        out.println("    <footer class=\"footer\">PeopleDesk &middot; Employee Management System</footer>");
        out.println("  </div>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /** Formats a date like "15 Jun 2022". */
    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    /** Formats a salary like "65,000.00". */
    public static String formatMoney(BigDecimal amount) {
        return amount == null ? "" : String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private static PrintWriter openWriter(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        return response.getWriter();
    }

    private static String head(String ctx, String title) {
        return "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "  <title>" + HtmlUtil.escape(title) + " - PeopleDesk</title>\n"
                + "  <link rel=\"stylesheet\" href=\"" + ctx + "/css/style.css\">\n"
                + "</head>";
    }

    private static String navLink(String href, String label, boolean active) {
        return "<a class=\"nav-link" + (active ? " active" : "") + "\" href=\"" + href + "\">"
                + HtmlUtil.escape(label) + "</a>";
    }
}
