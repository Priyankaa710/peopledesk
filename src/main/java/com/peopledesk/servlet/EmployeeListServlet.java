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

/** Lists all employees, or only the ones matching ?q=... (name or ID). URL: /employees */
public class EmployeeListServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EmployeeListServlet.class.getName());
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        String keyword = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        if (keyword.length() > 100) {
            keyword = keyword.substring(0, 100);
        }

        StringBuilder html = new StringBuilder();

        // Search form (GET, because searching does not change any data)
        html.append("<div class=\"card toolbar\">\n")
            .append("<form method=\"get\" action=\"").append(ctx).append("/employees\" class=\"search-form\">\n")
            .append("  <input type=\"text\" name=\"q\" value=\"").append(HtmlUtil.escape(keyword))
            .append("\" placeholder=\"Search by name or employee ID\" maxlength=\"100\" aria-label=\"Search employees\">\n")
            .append("  <button type=\"submit\" class=\"btn btn-primary\">Search</button>\n");
        if (!keyword.isEmpty()) {
            html.append("  <a class=\"btn btn-secondary\" href=\"").append(ctx).append("/employees\">Clear</a>\n");
        }
        html.append("</form>\n")
            .append("<a class=\"btn btn-primary\" href=\"").append(ctx).append("/employees/add\">+ Add Employee</a>\n")
            .append("</div>\n");

        try {
            List<Employee> employees = keyword.isEmpty() ? employeeDao.findAll() : employeeDao.search(keyword);

            html.append("<div class=\"card\">\n");
            if (employees.isEmpty()) {
                if (keyword.isEmpty()) {
                    html.append("<p class=\"empty\">There are no employees yet. <a href=\"").append(ctx)
                        .append("/employees/add\">Add the first employee</a>.</p>\n");
                } else {
                    html.append("<p class=\"empty\">No employees match &ldquo;").append(HtmlUtil.escape(keyword))
                        .append("&rdquo;. Try a different name or ID.</p>\n");
                }
            } else {
                html.append("<p class=\"result-count\">").append(employees.size())
                    .append(employees.size() == 1 ? " employee" : " employees")
                    .append(keyword.isEmpty() ? "" : " found").append("</p>\n");
                html.append("<div class=\"table-wrap\"><table class=\"table\">\n")
                    .append("<thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Department</th>")
                    .append("<th>Job Title</th><th class=\"num\">Salary</th><th>Joining Date</th><th>Actions</th></tr></thead>\n<tbody>\n");
                for (Employee e : employees) {
                    html.append("<tr>")
                        .append("<td>").append(e.getId()).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getName())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getEmail())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getPhone())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getDepartment())).append("</td>")
                        .append("<td>").append(HtmlUtil.escape(e.getJobTitle())).append("</td>")
                        .append("<td class=\"num\">").append(PageTemplate.formatMoney(e.getSalary())).append("</td>")
                        .append("<td>").append(PageTemplate.formatDate(e.getJoiningDate())).append("</td>")
                        .append("<td class=\"actions\">")
                        .append("<a class=\"btn btn-small btn-secondary\" href=\"").append(ctx)
                        .append("/employees/edit?id=").append(e.getId()).append("\">Edit</a> ")
                        .append("<a class=\"btn btn-small btn-danger\" href=\"").append(ctx)
                        .append("/employees/delete?id=").append(e.getId()).append("\">Delete</a>")
                        .append("</td></tr>\n");
                }
                html.append("</tbody></table></div>\n");
            }
            html.append("</div>\n");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not load employees", e);
            html.append(Flash.alert("error", "Employees could not be loaded right now. Please try again later."));
        }

        PageTemplate.renderApp(req, resp, "Employees", "employees", html.toString());
    }
}
