package com.peopledesk.servlet;

import com.peopledesk.dao.EmployeeDAO;
import com.peopledesk.model.Employee;
import com.peopledesk.util.Flash;
import com.peopledesk.util.HtmlUtil;
import com.peopledesk.util.PageTemplate;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Dashboard with live statistics from the database. URL: /dashboard */
public class DashboardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DashboardServlet.class.getName());
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        String username = String.valueOf(req.getSession().getAttribute("username"));
        StringBuilder html = new StringBuilder();

        html.append("<div class=\"card welcome-card\">\n")
            .append("  <h2>Welcome back, ").append(HtmlUtil.escape(username)).append("!</h2>\n")
            .append("  <p>Here is a quick overview of your employee records.</p>\n")
            .append("  <div class=\"quick-actions\">\n")
            .append("    <a class=\"btn btn-primary\" href=\"").append(ctx).append("/employees/add\">Add Employee</a>\n")
            .append("    <a class=\"btn btn-secondary\" href=\"").append(ctx).append("/employees\">View All Employees</a>\n")
            .append("  </div>\n")
            .append("</div>\n");

        try {
            int totalEmployees = employeeDao.countEmployees();
            int totalDepartments = employeeDao.countDepartments();
            List<Employee> recent = employeeDao.findRecent(5);

            html.append("<div class=\"stats\">\n")
                .append(statCard("Total Employees", totalEmployees))
                .append(statCard("Total Departments", totalDepartments))
                .append("</div>\n");

            html.append("<div class=\"card\">\n<div class=\"card-header\"><h2>Recently Added Employees</h2>")
                .append("<a href=\"").append(ctx).append("/employees\">View all</a></div>\n");

            if (recent.isEmpty()) {
                html.append("<p class=\"empty\">No employees yet. <a href=\"").append(ctx)
                    .append("/employees/add\">Add the first employee</a>.</p>\n");
            } else {
                html.append("<div class=\"table-wrap\"><table class=\"table\">\n")
                    .append("<thead><tr><th>ID</th><th>Name</th><th>Department</th><th>Job Title</th><th>Joined</th></tr></thead>\n<tbody>\n");
                for (Employee e : recent) {
                    html.append("<tr><td>").append(e.getId()).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getName())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getDepartment())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getJobTitle())).append("</td>")
                        .append("<td>").append(PageTemplate.formatDate(e.getJoiningDate())).append("</td></tr>\n");
                }
                html.append("</tbody></table></div>\n");
            }
            html.append("</div>\n");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not load dashboard data", e);
            html.append(Flash.alert("error", "Dashboard data could not be loaded right now. Please try again later."));
        }

        PageTemplate.renderApp(req, resp, "Dashboard", "dashboard", html.toString());
    }

    private String statCard(String label, int value) {
        return "<div class=\"stat-card\"><div class=\"stat-label\">" + label
                + "</div><div class=\"stat-value\">" + value + "</div></div>\n";
    }
}
