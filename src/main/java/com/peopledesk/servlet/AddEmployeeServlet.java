package com.peopledesk.servlet;

import com.peopledesk.dao.EmployeeDAO;
import com.peopledesk.model.Employee;
import com.peopledesk.util.EmployeeFormData;
import com.peopledesk.util.EmployeeFormView;
import com.peopledesk.util.Flash;
import com.peopledesk.util.HtmlUtil;
import com.peopledesk.util.PageTemplate;
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

/** Shows the "Add Employee" form (GET) and saves a new employee (POST). URL: /employees/add */
public class AddEmployeeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AddEmployeeServlet.class.getName());
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        showForm(req, resp, new EmployeeFormData(), new ArrayList<String>());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EmployeeFormData data = EmployeeFormData.fromRequest(req);

        // 1. Server-side validation (never trust the browser)
        List<String> errors = data.validate();

        if (errors.isEmpty()) {
            try {
                // 2. Friendly duplicate-email check
                if (employeeDao.emailExists(data.getEmail(), 0)) {
                    errors.add("An employee with the email " + data.getEmail() + " already exists.");
                } else {
                    // 3. Save to MySQL
                    Employee employee = data.toEmployee();
                    int newId = employeeDao.insert(employee);
                    Flash.success(req.getSession(),
                            "Employee \"" + employee.getName() + "\" was added successfully (ID " + newId + ").");
                    resp.sendRedirect(req.getContextPath() + "/employees");
                    return;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == EmployeeDAO.MYSQL_DUPLICATE_ENTRY) {
                    errors.add("An employee with the email " + data.getEmail() + " already exists.");
                } else {
                    LOG.log(Level.SEVERE, "Could not add employee", e);
                    errors.add("The employee could not be saved because of a database problem. Please try again.");
                }
            }
        }
        showForm(req, resp, data, errors);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, EmployeeFormData data,
                          List<String> errors) throws IOException {
        List<String> departments = new ArrayList<>();
        try {
            departments = employeeDao.findDepartments();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not load department suggestions", e);
        }
        String body = EmployeeFormView.render(req, req.getContextPath() + "/employees/add", data, null,
                departments, errors, "Save Employee");
        PageTemplate.renderApp(req, resp, "Add Employee", "add", body);
    }
}
