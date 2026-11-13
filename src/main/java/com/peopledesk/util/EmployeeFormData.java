package com.peopledesk.util;

import com.peopledesk.model.Employee;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

/**
 * Holds what the user typed into the employee form (as plain text) and checks it on the server.
 * Keeping the raw text lets us show the form again with the user's values when something is wrong.
 * Used by both the Add and the Edit servlets so the rules exist in only one place.
 */
public class EmployeeFormData {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 ()-]{6,18}$");
    private static final Pattern SALARY = Pattern.compile("^\\d{1,8}(\\.\\d{1,2})?$");
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1950, 1, 1);

    private String name = "";
    private String email = "";
    private String phone = "";
    private String department = "";
    private String jobTitle = "";
    private String salary = "";
    private String joiningDate = "";

    /** Reads the submitted form fields (trimmed). The email is stored in lower case. */
    public static EmployeeFormData fromRequest(HttpServletRequest request) {
        EmployeeFormData d = new EmployeeFormData();
        d.name = clean(request.getParameter("name"));
        d.email = clean(request.getParameter("email")).toLowerCase();
        d.phone = clean(request.getParameter("phone"));
        d.department = clean(request.getParameter("department"));
        d.jobTitle = clean(request.getParameter("jobTitle"));
        d.salary = clean(request.getParameter("salary"));
        d.joiningDate = clean(request.getParameter("joiningDate"));
        return d;
    }

    /** Fills the form data from an existing employee (used by the Edit page). */
    public static EmployeeFormData fromEmployee(Employee e) {
        EmployeeFormData d = new EmployeeFormData();
        d.name = e.getName();
        d.email = e.getEmail();
        d.phone = e.getPhone();
        d.department = e.getDepartment();
        d.jobTitle = e.getJobTitle();
        d.salary = e.getSalary() == null ? "" : e.getSalary().toPlainString();
        d.joiningDate = e.getJoiningDate() == null ? "" : e.getJoiningDate().toString();
        return d;
    }

    /** Returns a list of error messages. An empty list means the data is valid. */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (name.isEmpty()) {
            errors.add("Full name is required.");
        } else if (name.length() > 100) {
            errors.add("Full name must be 100 characters or fewer.");
        }

        if (email.isEmpty()) {
            errors.add("Email is required.");
        } else if (email.length() > 120 || !EMAIL.matcher(email).matches()) {
            errors.add("Please enter a valid email address (for example name@company.com).");
        }

        if (phone.isEmpty()) {
            errors.add("Phone number is required.");
        } else if (!PHONE.matcher(phone).matches()) {
            errors.add("Phone number may contain only digits, spaces, +, - and brackets (7 to 20 characters).");
        }

        if (department.isEmpty()) {
            errors.add("Department is required.");
        } else if (department.length() > 60) {
            errors.add("Department must be 60 characters or fewer.");
        }

        if (jobTitle.isEmpty()) {
            errors.add("Job title is required.");
        } else if (jobTitle.length() > 80) {
            errors.add("Job title must be 80 characters or fewer.");
        }

        if (salary.isEmpty()) {
            errors.add("Salary is required.");
        } else if (!SALARY.matcher(salary).matches()) {
            errors.add("Salary must be a number that is not negative, with at most 2 decimal places (for example 45000 or 45000.50).");
        }

        if (joiningDate.isEmpty()) {
            errors.add("Joining date is required.");
        } else {
            try {
                LocalDate date = LocalDate.parse(joiningDate);
                if (date.isBefore(EARLIEST_DATE) || date.isAfter(LocalDate.now().plusYears(1))) {
                    errors.add("Joining date must be between 1 January 1950 and one year from today.");
                }
            } catch (DateTimeException ex) {
                errors.add("Joining date must be a valid date.");
            }
        }
        return errors;
    }

    /** Converts to an Employee object. Call this ONLY after validate() returned no errors. */
    public Employee toEmployee() {
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        e.setPhone(phone);
        e.setDepartment(department);
        e.setJobTitle(jobTitle);
        e.setSalary(new BigDecimal(salary));
        e.setJoiningDate(LocalDate.parse(joiningDate));
        return e;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public String getJobTitle() { return jobTitle; }
    public String getSalary() { return salary; }
    public String getJoiningDate() { return joiningDate; }
}
