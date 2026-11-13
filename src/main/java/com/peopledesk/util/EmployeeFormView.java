package com.peopledesk.util;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;

/** Builds the HTML of the employee form. Shared by the Add page and the Edit page. */
public final class EmployeeFormView {

    private static final String[] DEFAULT_DEPARTMENTS = {
        "Engineering", "Human Resources", "Finance", "Sales", "Marketing", "Operations", "IT Support"
    };

    private EmployeeFormView() {
    }

    /**
     * @param action      URL the form is sent to (POST)
     * @param data        values to show in the fields
     * @param employeeId  the employee's ID when editing, or null when adding
     * @param departments department names that already exist in the database (shown as suggestions)
     * @param errors      validation errors to show above the form (may be empty)
     * @param submitLabel text of the submit button
     */
    public static String render(HttpServletRequest request, String action, EmployeeFormData data,
                                Integer employeeId, List<String> departments,
                                List<String> errors, String submitLabel) {
        String ctx = request.getContextPath();
        StringBuilder sb = new StringBuilder();

        if (errors != null && !errors.isEmpty()) {
            sb.append("<div class=\"alert alert-error\" role=\"alert\">\n");
            sb.append("  <strong>Please fix the following:</strong>\n  <ul>\n");
            for (String error : errors) {
                sb.append("    <li>").append(HtmlUtil.escape(error)).append("</li>\n");
            }
            sb.append("  </ul>\n</div>\n");
        }

        Set<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String d : DEFAULT_DEPARTMENTS) {
            suggestions.add(d);
        }
        if (departments != null) {
            suggestions.addAll(departments);
        }

        sb.append("<div class=\"card\">\n");
        sb.append("<form method=\"post\" action=\"").append(HtmlUtil.escape(action)).append("\" class=\"form\">\n");
        sb.append(CsrfUtil.hiddenField(request.getSession())).append("\n");
        if (employeeId != null) {
            sb.append("<input type=\"hidden\" name=\"id\" value=\"").append(employeeId).append("\">\n");
        }

        sb.append("<div class=\"form-grid\">\n");

        sb.append("<div class=\"form-group\">\n<label for=\"employeeId\">Employee ID</label>\n");
        sb.append("<input type=\"text\" id=\"employeeId\" value=\"")
          .append(employeeId == null ? "Generated automatically" : String.valueOf(employeeId))
          .append("\" disabled>\n</div>\n");

        sb.append(textField("name", "Full name", "text", data.getName(), "maxlength=\"100\" required"));
        sb.append(textField("email", "Email", "email", data.getEmail(), "maxlength=\"120\" required"));
        sb.append(textField("phone", "Phone number", "tel", data.getPhone(), "maxlength=\"20\" required"));
        sb.append(textField("department", "Department", "text", data.getDepartment(),
                "maxlength=\"60\" list=\"departmentList\" required"));
        sb.append(textField("jobTitle", "Job title", "text", data.getJobTitle(), "maxlength=\"80\" required"));
        sb.append(textField("salary", "Salary", "number", data.getSalary(), "min=\"0\" step=\"0.01\" required"));
        sb.append(textField("joiningDate", "Joining date", "date", data.getJoiningDate(), "required"));

        sb.append("</div>\n");

        sb.append("<datalist id=\"departmentList\">\n");
        for (String d : suggestions) {
            sb.append("  <option value=\"").append(HtmlUtil.escape(d)).append("\">\n");
        }
        sb.append("</datalist>\n");

        sb.append("<div class=\"form-actions\">\n");
        sb.append("  <button type=\"submit\" class=\"btn btn-primary\">").append(HtmlUtil.escape(submitLabel)).append("</button>\n");
        sb.append("  <a class=\"btn btn-secondary\" href=\"").append(ctx).append("/employees\">Cancel</a>\n");
        sb.append("</div>\n");
        sb.append("</form>\n</div>\n");
        return sb.toString();
    }

    private static String textField(String name, String label, String type, String value, String extraAttributes) {
        return "<div class=\"form-group\">\n"
                + "<label for=\"" + name + "\">" + label + "</label>\n"
                + "<input type=\"" + type + "\" id=\"" + name + "\" name=\"" + name + "\" value=\""
                + HtmlUtil.escape(value) + "\" " + extraAttributes + ">\n"
                + "</div>\n";
    }
}
