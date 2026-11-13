# PeopleDesk – Employee Management System

## About the Project

PeopleDesk is a simple employee management web application developed using Java Servlets, JDBC and MySQL. It helps an authorised user manage employee information from a single place.

The application includes login, a dashboard and options to add, view, search, update and delete employee records. It uses HTML and CSS for the user interface and runs on Apache Tomcat 9.

This project was developed for learning and interview preparation.

## Problem Statement

Managing employee details in spreadsheets can make it difficult to search, update and maintain records. PeopleDesk provides a single application where employee information can be stored and managed through a database.

## Objectives

- To understand Java Servlets and JDBC.
- To connect a Java web application with MySQL.
- To perform CRUD operations on employee records.
- To learn session management and login validation.
- To practise input validation and safe database queries.
- To understand how to build and deploy a WAR file using Maven and Tomcat.

## Features

- **Login:** Allows authorised users to access the application.
- **Initial Setup:** Creates the first administrator account when no user exists.
- **Dashboard:** Displays employee statistics and recently added records.
- **Add Employee:** Saves employee details after validation.
- **View Employees:** Displays employee records in a table.
- **Search:** Searches employees by name or employee ID.
- **Edit Employee:** Updates existing employee details.
- **Delete Employee:** Deletes a record after confirmation.
- **Logout:** Ends the current login session.

## Technologies Used

| Technology | Purpose |
|---|---|
| Core Java | Application logic and object-oriented programming |
| Java Servlets | Handling browser requests |
| JDBC | Connecting Java with MySQL |
| MySQL 8 | Storing employee and user data |
| HTML5 | Creating web pages and forms |
| CSS3 | Styling the application |
| Maven | Building the project |
| Apache Tomcat 9 | Running the web application |

The project uses plain Java Servlets and JDBC. It does not use Spring Boot, Hibernate, Bootstrap or JavaScript.

## Application Structure

The project follows a simple layered structure to keep the code organised.

- **Model:** Contains classes such as `Employee` and `User`.
- **DAO:** Contains database operations for employees and users.
- **Servlets:** Handle login, dashboard and employee-related requests.
- **Filters:** Check authentication and request requirements.
- **Utility Classes:** Handle database configuration, password hashing, validation and HTML output.
- **Web Pages:** Contain the HTML entry page and CSS stylesheet.

### Application Flow

Browser → Servlet → DAO → JDBC → MySQL

When a user submits a form, the servlet processes the request, the DAO communicates with the database, and the application returns the appropriate page.

## Project Folder Structure

```text
PeopleDesk/
├── pom.xml
├── README.md
├── INTERVIEW_NOTES.md
├── database.sql
├── config.example.properties
├── .gitignore
└── src/
    └── main/
        ├── java/com/peopledesk/
        │   ├── config/
        │   ├── model/
        │   ├── dao/
        │   ├── servlet/
        │   ├── filter/
        │   └── util/
        ├── resources/
        │   └── config.properties
        └── webapp/
            ├── index.html
            ├── css/
            │   └── style.css
            └── WEB-INF/
                └── web.xml
```

`config.properties` is created locally and should not be committed with database credentials.

## Requirements

Install the following software before running the project:

1. JDK 17 or later, compatible with the configured Maven compiler.
2. Apache Maven 3.8 or later.
3. MySQL Server 8.x.
4. MySQL Workbench.
5. Apache Tomcat 9.

**Important:** Use Tomcat 9 because this application uses the `javax.servlet` API. Tomcat 10 and later use `jakarta.servlet`.

## Database Setup

1. Open MySQL Workbench and connect to your local MySQL server.
2. Open the `database.sql` file from the project folder.
3. Execute the SQL script.
4. Refresh the Schemas panel.
5. Confirm that the `peopledesk` database contains the `employees` and `users` tables.

To check the sample employee records, run:

```sql
SELECT * FROM peopledesk.employees;
```

The supplied SQL script inserts sample employee records. It does not create a login account.

## Database Configuration

Create the configuration file from the example file.

In PowerShell, run these commands from the project folder:

```powershell
Copy-Item config.example.properties src\main\resources\config.properties
```

Open `src/main/resources/config.properties` and update the database details:

```properties
db.url=jdbc:mysql://localhost:3306/peopledesk?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

Replace `YOUR_MYSQL_PASSWORD` with the password for your own MySQL account. Do not share this file publicly.

After changing the configuration, rebuild the project so the updated file is included in the WAR.

## Create the First Administrator

The project does not have a default username and password.

1. Start the application on Tomcat.
2. Open `http://localhost:8080/PeopleDesk/setup`.
3. Enter a username and a password of at least eight characters.
4. Confirm the password and submit the form.
5. Open the login page and sign in using the credentials you created.

The setup page works only while the `users` table is empty. The password is stored as a hash rather than as plain text.

For local development, if you forget the password, you can reset the initial setup by clearing the users table in MySQL Workbench:

```sql
DELETE FROM peopledesk.users;
```

**Warning:** This deletes all user accounts in that table. Use it only when you understand the effect and want to recreate the administrator.

## Build the Project

Open PowerShell and move to the folder containing `pom.xml`.

```powershell
mvn clean package
```

When the build completes successfully, Maven creates the WAR file here:

```text
target/PeopleDesk.war
```

## Run the Application

Copy the WAR file to the Tomcat webapps folder:

```powershell
Copy-Item target\PeopleDesk.war "C:\apache-tomcat-9.0.117\webapps\" -Force
```

Start Tomcat:

```powershell
& "C:\apache-tomcat-9.0.117\bin\startup.bat"
```

Wait for Tomcat to finish starting, then open:

- Application: `http://localhost:8080/PeopleDesk/`
- Initial administrator setup: `http://localhost:8080/PeopleDesk/setup`

After creating the administrator account, log in and try the dashboard and employee management features.

If you update the code, rebuild the WAR and redeploy the updated file. If the old version remains deployed, stop Tomcat and replace both the old WAR and its extracted application folder before restarting.

## Employee Management Workflow

| Operation | Description |
|---|---|
| Create | Enter employee details and save the record. |
| Read | View employee information in the employee list. |
| Search | Find an employee by name or ID. |
| Update | Edit an existing employee's information. |
| Delete | Confirm and delete an employee record. |

The application validates form data on the server and checks for duplicate email addresses before saving a new employee.

## Security and Validation

The project includes the following security measures:

- Password hashing using PBKDF2.
- `PreparedStatement` for database queries.
- Session-based login and protected pages.
- Session invalidation during logout.
- CSRF token checks for POST requests.
- Server-side validation of employee details.
- HTML escaping to reduce the risk of cross-site scripting (XSS).
- Unique constraints for usernames and employee email addresses.
- Database credentials kept outside the source-controlled project files.

These measures are intended for learning and do not mean that the application has undergone a professional security audit.

## Troubleshooting

| Problem | What to check |
|---|---|
| `mvn` is not recognised | Check Maven's `bin` folder in the system Path. |
| Build fails | Check the first Maven error and verify the Java version used by Maven. |
| Application shows 404 | Confirm that `PeopleDesk.war` is deployed and Tomcat has started. |
| Database connection fails | Check that MySQL is running and the configuration values are correct. |
| Access denied by MySQL | Verify the database username and password. |
| Unknown database or table | Run `database.sql` in MySQL Workbench. |
| Login says invalid username or password | Check the username and password created during initial setup. |
| CSS is not loading | Check the browser URL and confirm that `src/main/webapp/css/style.css` is included in the deployed application. |
| Configuration changes have no effect | Rebuild the WAR and redeploy it to Tomcat. |

For server-side errors, check the logs in the Tomcat `logs` folder.

## Current Project Status

The Maven build completed successfully in the local development environment, compiling the Java source files and creating `PeopleDesk.war`.

Database configuration, deployment and browser behaviour should be verified on the machine where the application is running. A successful build alone does not confirm that every feature works correctly.

## Known Limitations

- There is no login-attempt limit or account lockout.
- All logged-in users currently have the same permissions.
- Additional user management and password-change pages are not included.
- The application uses HTTP for local development; real deployment should use HTTPS.
- Employee records are displayed without pagination.
- Department names are stored as text rather than in a separate department table.
- There is no audit history or automated test suite.
- The application has not undergone a professional security audit.

## Future Improvements

Possible improvements include:

- Adding separate roles for HR staff and read-only users.
- Adding password change and user management features.
- Adding pagination and sorting to the employee list.
- Creating a separate department table.
- Adding automated unit and integration tests.
- Adding CSV export and employee history.
- Using a connection pool for database access.
- Deploying behind HTTPS with production security settings.

## Learning Outcomes

Through this project, I practised Java web application development, JDBC database connectivity, SQL CRUD operations, session management, form validation and deployment using Maven and Apache Tomcat.

This project helped me understand how the different parts of a traditional Java web application work together.
