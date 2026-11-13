package com.peopledesk.servlet;

import com.peopledesk.dao.EmployeeDAO;
import com.peopledesk.model.Employee;
import com.peopledesk.util.EmployeeFormData;
import com.peopledesk.util.EmployeeFormView;
import com.peopledesk.util.Flash;
import com.peopledesk.util.PageTemplate;
import com.peopledesk.util.RequestUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Shows the form with current values (GET ?id=5) and saves the changes (POST). URL: /employees/edit */
public class EditEmployeeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EditEmployeeServlet.class.getName());
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = RequestUtil.parseId(req.getParameter("id"));
        try {
            Employee employee = id > 0 ? employeeDao.findById(id) : null;
            if (employee == null) {
                notFound(req, resp);
                return;
            }
            showForm(req, resp, EmployeeFormData.fromEmployee(employee), id, new ArrayList<String>());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not load employee " + id, e);
            Flash.error(req.getSession(), "The employee could not be loaded right now. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/employees");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = RequestUtil.parseId(req.getParameter("id"));
        if (id < 0) {
            notFound(req, resp);
            return;
        }

        EmployeeFormData data = EmployeeFormData.fromRequest(req);
        List<String> errors = data.validate();

        if (errors.isEmpty()) {
            try {
                if (employeeDao.emailExists(data.getEmail(), id)) {
                    errors.add("Another employee already uses the email " + data.getEmail() + ".");
                } else {
                    Employee employee = data.toEmployee();
                    employee.setId(id);
                    if (employeeDao.update(employee)) {
                        Flash.success(req.getSession(), "Employee \"" + employee.getName() + "\" was updated successfully.");
                        resp.sendRedirect(req.getContextPath() + "/employees");
                    } else {
                        notFound(req, resp); // deleted by someone else in the meantime
                    }
                    return;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == EmployeeDAO.MYSQL_DUPLICATE_ENTRY) {
                    errors.add("Another employee already uses the email " + data.getEmail() + ".");
                } else {
                    LOG.log(Level.SEVERE, "Could not update employee " + id, e);
                    errors.add("The changes could not be saved because of a database problem. Please try again.");
                }
            }
        }
        showForm(req, resp, data, id, errors);
    }

    private void notFound(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Flash.error(req.getSession(), "That employee does not exist (it may have been deleted).");
        resp.sendRedirect(req.getContextPath() + "/employees");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, EmployeeFormData data, int id,
                          List<String> errors) throws IOException {
        List<String> departments = new ArrayList<>();
        try {
            departments = employeeDao.findDepartments();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not load department suggestions", e);
        }
        String body = EmployeeFormView.render(req, req.getContextPath() + "/employees/edit", data, id,
                departments, errors, "Update Employee");
        PageTemplate.renderApp(req, resp, "Edit Employee", "employees", body);
    }
}
