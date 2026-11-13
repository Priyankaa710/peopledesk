package com.peopledesk.servlet;

import com.peopledesk.dao.EmployeeDAO;
import com.peopledesk.model.Employee;
import com.peopledesk.util.CsrfUtil;
import com.peopledesk.util.Flash;
import com.peopledesk.util.HtmlUtil;
import com.peopledesk.util.PageTemplate;
import com.peopledesk.util.RequestUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Two steps, so nothing is deleted by accident:
 *   GET  /employees/delete?id=5  -> confirmation page (changes nothing)
 *   POST /employees/delete       -> really deletes the employee
 */
public class DeleteEmployeeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DeleteEmployeeServlet.class.getName());
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        int id = RequestUtil.parseId(req.getParameter("id"));
        try {
            Employee e = id > 0 ? employeeDao.findById(id) : null;
            if (e == null) {
                Flash.error(req.getSession(), "That employee does not exist (it may have been deleted).");
                resp.sendRedirect(ctx + "/employees");
                return;
            }

            String body = "<div class=\"card confirm-card\">\n"
                    + "<p>Are you sure you want to delete this employee? <strong>This cannot be undone.</strong></p>\n"
                    + "<dl class=\"details\">\n"
                    + "<dt>Employee ID</dt><dd>" + e.getId() + "</dd>\n"
                    + "<dt>Name</dt><dd>" + HtmlUtil.escape(e.getName()) + "</dd>\n"
                    + "<dt>Email</dt><dd>" + HtmlUtil.escape(e.getEmail()) + "</dd>\n"
                    + "<dt>Department</dt><dd>" + HtmlUtil.escape(e.getDepartment()) + "</dd>\n"
                    + "<dt>Job title</dt><dd>" + HtmlUtil.escape(e.getJobTitle()) + "</dd>\n"
                    + "</dl>\n"
                    + "<form method=\"post\" action=\"" + ctx + "/employees/delete\" class=\"form-actions\">\n"
                    + CsrfUtil.hiddenField(req.getSession()) + "\n"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + e.getId() + "\">\n"
                    + "<button type=\"submit\" class=\"btn btn-danger-solid\">Yes, delete employee</button>\n"
                    + "<a class=\"btn btn-secondary\" href=\"" + ctx + "/employees\">Cancel</a>\n"
                    + "</form>\n</div>\n";
            PageTemplate.renderApp(req, resp, "Delete Employee", "employees", body);

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Could not load employee " + id + " for deletion", ex);
            Flash.error(req.getSession(), "The employee could not be loaded right now. Please try again.");
            resp.sendRedirect(ctx + "/employees");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = RequestUtil.parseId(req.getParameter("id"));
        try {
            if (id > 0 && employeeDao.delete(id)) {
                Flash.success(req.getSession(), "Employee (ID " + id + ") was deleted successfully.");
            } else {
                Flash.error(req.getSession(), "That employee does not exist (it may have been deleted already).");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not delete employee " + id, e);
            Flash.error(req.getSession(), "The employee could not be deleted because of a database problem.");
        }
        resp.sendRedirect(req.getContextPath() + "/employees");
    }
}
