# PeopleDesk — Interview Notes

Simple English, short answers. **Change the wording so it sounds like you and is true for you.** Read the code while you read these notes — you will explain it much better if you have seen every file at least once.

---

## A. 60-second project introduction

> "PeopleDesk is an employee management web application. I built it with Java Servlets, JDBC and MySQL, and it runs on Apache Tomcat 9. I did not use Spring, because I wanted to understand the basics of how a Java web application really works.
>
> A user logs in with a username and password. The password is stored as a salted hash, not as plain text. After login the user sees a dashboard with the number of employees and departments, taken live from the database. The user can add, search, edit and delete employees.
>
> The code is split into layers: model classes for the data, DAO classes for SQL, servlets for requests, and a filter that blocks anyone who is not logged in. I used PreparedStatement for every query, I validate input on the server, and I added a CSRF token to the forms. The project is built with Maven into a WAR file."

(About 150 words — roughly one minute when spoken calmly.)

---

## B. Short explanations of the main topics

**Project purpose** — Keep employee records in one database behind a login, so they are easy to search and manage.

**Application architecture** — Request → Filter → Servlet → DAO → MySQL, then the servlet writes the HTML response. Each layer has one job:
- *Model*: `Employee`, `User` — just data.
- *DAO*: `EmployeeDAO`, `UserDAO` — all SQL.
- *Servlet*: reads the request, calls validation and DAO, sends the page.
- *Filter*: checks login and CSRF before any servlet runs.
- *Util*: helpers (password hashing, HTML escaping, page layout).

**HTML and CSS** — HTML builds the pages and forms. One CSS file (`css/style.css`) gives every page the same look. No JavaScript, no framework.

**Java Servlets** — Java classes that receive HTTP requests and create responses. Each servlet is mapped to a URL in `web.xml` (for example `/employees/add`). Tomcat creates one servlet object and uses it for many requests.

**JDBC** — Java's standard way to talk to a database. Steps: get a `Connection` → create a `PreparedStatement` → execute → read the `ResultSet` → close everything.

**MySQL** — The relational database. Two tables: `users` and `employees`. `email` and `username` have UNIQUE keys.

**PreparedStatement** — SQL with `?` placeholders. The values are sent separately from the SQL text, so user input can never change the meaning of the query. This stops SQL injection.

**HttpSession** — Server-side memory for one user. After login I store `userId`, `username` and `role` in the session. The browser only keeps a cookie (`JSESSIONID`) with a random id.

**AuthenticationFilter** — Runs before every request. Public pages (login, setup, CSS) pass. Everything else needs a session with `userId`; otherwise it redirects to `/login`. It also checks the CSRF token on every POST.

**CRUD operations** — Create (add), Read (list/search), Update (edit), Delete. In SQL: `INSERT`, `SELECT`, `UPDATE`, `DELETE`.

**GET versus POST** — GET is for reading; it puts data in the URL and must not change anything. POST is for changing data; the data is in the request body. Logout, add, edit and delete are all POST.

**DAO pattern** — Data Access Object. All database code for one table lives in one class, so servlets never contain SQL. If the database changes, only the DAO changes.

**Password hashing** — I never store the password. I store `pbkdf2_sha256$iterations$salt$hash`. At login I hash the typed password with the same salt and compare. A hash cannot be turned back into the password.

**Maven** — A build tool. `pom.xml` lists dependencies and settings. `mvn clean package` compiles the code and creates `target/PeopleDesk.war`.

**Apache Tomcat** — A web server and servlet container. It receives HTTP requests, runs servlets and filters, and manages sessions. I deploy the WAR into its `webapps` folder.

---

## C. Login process, step by step

1. User opens `/PeopleDesk/` → `index.html` redirects to `/login`.
2. `LoginServlet.doGet` creates a session and shows the form. The form contains a hidden CSRF token.
3. User types username and password and clicks **Login** (`POST /login`).
4. `AuthenticationFilter` sees a POST and checks the CSRF token. If it is wrong → 403.
5. `LoginServlet.doPost` checks that both fields are not empty.
6. `UserDAO.findByUsername` runs `SELECT ... WHERE username = ?` with a PreparedStatement.
7. `PasswordUtil.verify` hashes the typed password with the stored salt and compares it with the stored hash.
8. If wrong (or user not found) → show "Invalid username or password" (same message in both cases).
9. If correct → the old session is invalidated, a **new** session is created, and `userId`, `username`, `role` are saved in it.
10. The servlet redirects to `/dashboard`.

## D. Logout process, step by step

1. The Logout button is a form with `method="post"` and a CSRF token.
2. `AuthenticationFilter` allows it (user is logged in, token valid).
3. `LogoutServlet.doPost` calls `session.invalidate()` — everything in the session is removed.
4. It redirects to `/login?msg=loggedout`.
5. If the user now types `/dashboard`, the filter finds no session and redirects to `/login`.
6. Protected pages are sent with `Cache-Control: no-store`, so the Back button does not show old employee data.

## E. Adding an employee — from the HTML form to MySQL

1. User clicks **Add Employee** → `GET /employees/add` → `AddEmployeeServlet.doGet` shows the form (built by `EmployeeFormView`).
2. User fills the form and clicks **Save** → `POST /employees/add`.
3. Filter checks login and CSRF token.
4. `EmployeeFormData.fromRequest` reads and trims the fields.
5. `validate()` checks required fields, email format, phone, salary (not negative, max 2 decimals), date. If there are errors, the form is shown again with the user's values and the error list.
6. `EmployeeDAO.emailExists` checks for a duplicate email.
7. `EmployeeDAO.insert` runs `INSERT INTO employees (...) VALUES (?, ?, ?, ?, ?, ?, ?)`. MySQL generates the `id`.
8. Success message is stored in the session ("flash") and the servlet redirects to `/employees`.
9. The list page shows the green success message once, and the new employee appears in the table.

## F. Updating and deleting

**Update**
1. Click **Edit** → `GET /employees/edit?id=5`.
2. `EmployeeDAO.findById(5)` loads the row. If it does not exist → error message and redirect to the list.
3. The form shows the current values. User changes them and submits (`POST`).
4. Same validation as Add. The duplicate-email check ignores the employee's own row (`id <> ?`).
5. `EmployeeDAO.update` runs `UPDATE ... WHERE id = ?`. If 0 rows changed, the employee no longer exists → error message.
6. Redirect to the list with a success message.

**Delete**
1. Click **Delete** → `GET /employees/delete?id=5` shows a **confirmation page**. Nothing is deleted yet.
2. Click **Yes, delete employee** → `POST /employees/delete` (with CSRF token).
3. `EmployeeDAO.delete` runs `DELETE FROM employees WHERE id = ?`.
4. If no row was deleted, the user sees "does not exist"; otherwise a success message.

---

## G. Interview questions and short answers

**1. What is a Servlet?**
A Java class that handles HTTP requests and creates responses. It extends `HttpServlet` and overrides methods like `doGet` and `doPost`.

**2. Explain the servlet life cycle.**
Tomcat loads the class and creates one object, calls `init()` once, calls `service()` (which calls `doGet`/`doPost`) for every request, and calls `destroy()` when the app stops.

**3. GET vs POST?**
GET reads data and shows parameters in the URL; it should not change anything. POST sends data in the body and is used to create, update or delete data.

**4. `sendRedirect` vs `forward`?**
`sendRedirect` tells the browser to make a new request (the URL changes). `forward` happens inside the server (the URL stays the same). I use redirect after a successful POST so that refreshing the page does not submit the form again.

**5. What is JDBC? What are the steps?**
The Java API for databases. Load driver → get `Connection` → create `PreparedStatement` → execute → read `ResultSet` → close resources.

**6. Statement vs PreparedStatement?**
`Statement` joins user input into the SQL text, which allows SQL injection. `PreparedStatement` uses `?` placeholders, so input is only data. It is also faster when repeated.

**7. What is SQL injection?**
Attacker input like `' OR '1'='1` changes the SQL query. I prevent it with PreparedStatement everywhere.

**8. What is HttpSession and how does it work?**
It stores data for one user on the server. Tomcat gives the browser a `JSESSIONID` cookie; on every request the cookie finds the right session. Mine times out after 30 minutes without activity.

**9. Filter vs Servlet?**
A filter runs before (and after) servlets and can stop or change a request; it is good for cross-cutting work like login checks. A servlet produces the actual response.

**10. Why use the DAO pattern?**
It keeps SQL in one place, makes servlets shorter and easier to read, and makes it easy to change the database code later.

**11. Why hash passwords? What is a salt?**
If the database leaks, plain passwords would be exposed. A hash is one-way. A salt is random data added before hashing, so two users with the same password get different hashes and ready-made hash tables do not work. I use PBKDF2 with many iterations to make guessing slow. MD5 or plain SHA-1 are too fast and not suitable for passwords.

**12. What is `web.xml`?**
The deployment descriptor. It maps URLs to servlets and filters, sets the session timeout, and defines error pages and the welcome file.

**13. What is a WAR file?**
Web Application Archive — a ZIP with a fixed layout (`WEB-INF/classes`, `WEB-INF/lib`, `WEB-INF/web.xml`, static files). Tomcat deploys it.

**14. What does Maven do? What does `provided` scope mean?**
Maven manages dependencies and builds the project. `provided` means "needed to compile, but the server already has it". The Servlet API is `provided` because Tomcat supplies it; including it in the WAR could cause conflicts.

**15. What is XSS and how did you prevent it?**
Cross-Site Scripting: an attacker saves text like `<script>...</script>` and it runs in other users' browsers. I escape every user-provided value with `HtmlUtil.escape` before writing it into HTML.

**16. What is CSRF and how did you prevent it?**
A malicious site tricks your logged-in browser into sending a request to my app. I put a random token in every form and check on the server that it matches the token stored in the session. Another site cannot read that token.

**17. Why is logout a POST request?**
Logout changes state. If it were a GET link, another website could log you out just by loading an image or link.

**18. Why do you redirect after POST? (Post/Redirect/Get)**
So that pressing refresh does not send the form again and create a duplicate record.

**19. What is try-with-resources?**
A Java feature that closes `Connection`, `PreparedStatement` and `ResultSet` automatically, even if an exception occurs. It prevents connection leaks.

**20. Tomcat 9 vs Tomcat 10?**
Tomcat 9 uses `javax.servlet.*`. Tomcat 10 and later use `jakarta.servlet.*`. My code uses `javax`, so it needs Tomcat 9.

**21. Primary key vs unique key?**
Primary key identifies a row (one per table, not null). A unique key prevents duplicate values in a column (for example `email`), and a table can have several.

**22. What is a connection pool, and does your project use one?**
A pool keeps database connections open and reuses them. My project opens a new connection per query, which is simple and fine for a small demo, but a pool (HikariCP or a Tomcat `DataSource`) would be better for real traffic.

**23. Why didn't you use Spring Boot?**
I wanted to learn what a framework hides: request handling, sessions, JDBC and security basics. Spring would do much of this for me; knowing the basics helps me use Spring well later.

---

## H. Five practical questions about the code

**1. "Show me where the SQL for searching is, and how it avoids injection."**
`EmployeeDAO.search`. The SQL is `WHERE name LIKE ?` (plus `OR id = ?` if the text is a number). The user's text is passed with `ps.setString(...)`, never added into the SQL string. I also escape `%` and `_` so they are treated as normal characters.

**2. "What happens if two people add the same email at the same time?"**
The servlet checks `emailExists` first and shows a friendly message. But two requests could pass that check together, so the database has a `UNIQUE` key on `email` as the real protection. If MySQL rejects the insert (error code 1062), the servlet catches the `SQLException` and shows the same "already exists" message.

**3. "How does the filter know which pages are public? What if I add a new page?"**
`AuthenticationFilter.isPublic()` lists the public paths (`/login`, `/setup`, `/error`, `/css/...`). Everything else is protected by default. So a new page is automatically protected — I would only edit that method if the new page should be public.

**4. "How would you add an 'address' field?"**
(1) `ALTER TABLE employees ADD address VARCHAR(200)` and update `database.sql`; (2) add the field to `Employee`; (3) add it to `EmployeeFormData` (read + validate) and `EmployeeFormView` (input box); (4) add it to the SQL in `EmployeeDAO` (`COLUMNS`, `insert`, `update`, `mapRow`); (5) show it in `EmployeeListServlet`.

**5. "Where is the CSRF token created and checked?"**
Created in `CsrfUtil.getToken(session)` and printed into forms by `CsrfUtil.hiddenField`. Checked in `AuthenticationFilter` for every POST by `CsrfUtil.isValid(request)`, which compares the form's token with the session's token.

*(Extra practice: "Why `LocalDate` instead of `java.sql.Date`?" — `LocalDate` has no time zone, so the date I save is exactly the date shown.)*

---

## I. What I Learned from This Project

*(Edit this so it matches what you really learned — you can keep these points only after you have studied the code.)*

- How an HTTP request travels through a filter and a servlet and comes back as an HTML response.
- How to connect Java to MySQL with JDBC, and why `PreparedStatement` and try-with-resources matter.
- How sessions work, and how login, logout and protected pages are built without a framework.
- Why passwords must be hashed with a salt, and why plain-text passwords are never acceptable.
- Basic web security: SQL injection, XSS and CSRF — what each one is and how to defend against it.
- How to organise code in layers (model, DAO, servlet, filter, util) and keep validation in one place.
- How a Maven project is structured, how `pom.xml` scopes work, and how a WAR is deployed to Tomcat.
- The difference between Tomcat 9 (`javax`) and Tomcat 10 (`jakarta`).
- Good habits: redirect after POST, friendly error messages, no secrets in source code.

---

## J. How to Explain My Contribution Honestly

Interviewers can tell quickly if you understand your own project. Honesty is the safest and strongest approach.

**This project was generated with the help of an AI assistant.** Do not hide that. A good, truthful way to say it:

> "I used an AI assistant to generate the first version of this project from a detailed specification I wrote. After that, I set it up on my laptop, ran it, read through the code, and learned how each part works. I can explain the login flow, the DAO layer and the security choices, and I also [made this change / fixed this problem — only say what is true]."

**Things you can honestly claim** (only after you really do them):
- You chose the technology stack and the feature list.
- You installed and configured Java, Maven, MySQL and Tomcat, and ran the application.
- You understand and can explain the code in this notes file.
- You made your own changes (see below).

**How to make it truly yours before the interview** (do at least two, it takes under an hour):
1. Add a new field (for example `address`) — the steps are in question H4.
2. Change something visible: the colours in `style.css`, or the text on the login page.
3. Put a breakpoint or a `System.out.println` in `LoginServlet` and watch one login happen.
4. Break something on purpose (wrong DB password, wrong URL) and find the error in the Tomcat log.
5. Try a SQL injection text and a `<script>` text in the forms, and explain why they fail.

**Do not say:**
- "I wrote every line myself." (not true)
- "This is production-ready / completely secure." (it is a learning project — see *Known limitations* in the README)
- Anything about a feature you cannot show in the code.

**If you don't know an answer:** say so, and explain how you would find out. "I haven't used that yet, but I would look at …" is a good answer.

---

## Quick 5-minute demo plan

1. Show the folder structure (model / dao / servlet / filter / util) — 30 seconds.
2. Open the login page. Try a wrong password (error message), then log in.
3. Dashboard: point out that the numbers come from MySQL.
4. Add an employee. Try an invalid email to show server-side validation. Try a duplicate email.
5. Search by name and by ID. Edit one employee. Delete one (show the confirmation page).
6. Click **Logout**, then press the browser's Back button and try to open `/PeopleDesk/employees` — you are sent to the login page.
7. Open `EmployeeDAO.java` and show a `PreparedStatement`. Open `AuthenticationFilter.java` and explain it.

**Before the interview:** start MySQL, start Tomcat, log in once, and keep the Tomcat log window open in case you need to show a message.
