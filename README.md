# Sunrise Dental Clinic — Project Skeleton

This is a starting skeleton for CIS6003 Task B, matching the class diagram
we designed earlier. It compiles and deploys, but the actual business
logic is left as TODOs for you to design and implement yourself.

## Stack
- **Frontend**: plain HTML/CSS/JavaScript (`src/main/webapp`)
- **Backend**: Java Servlets on Apache Tomcat (`src/main/java`)
- **Database**: MySQL via JDBC (`database/schema.sql`)

## Project layout
```
src/main/java/com/sunrise/dental/
  model/     Patient, Dentist, Appointment, Bill  (matches class diagram)
  dao/       DAO pattern - AppointmentDAO (interface + impl), PatientDAO, BillDAO
  db/        DBConnection - Singleton pattern
  service/   AppointmentManager - business logic / controller tier
  servlet/   Web service endpoints (the "distributed application" layer)
src/main/webapp/
  index.html, css/style.css, js/app.js   Frontend
database/schema.sql                       MySQL schema + seed data
```

## What's already done for you (infrastructure, not graded logic)
- Maven project setup (`pom.xml`)
- `DBConnection` singleton (credentials via environment variables, not hardcoded)
- All servlets wired up to read requests / return JSON
- Server-side session authentication: `AuthFilter` blocks every `/api/*`
  request except `/api/login` unless the caller has a valid logged-in
  session (previously the login screen was only a client-side UI gate —
  someone could call the API directly without logging in). `LoginServlet`
  creates the session on success; `LogoutServlet` invalidates it.
- Basic frontend with working login screen and tab navigation
- Database schema matching the brief's required fields

## What you need to design and implement (this is the graded part)
Search the codebase for `TODO` comments. The main ones:
1. **`Appointment.validate()`** — your input validation rules
2. **`Appointment.generateApptNumber()`** — how appointment numbers are generated
3. **`Bill.calculateTotal()`** — how treatment cost + consultation fee combine
4. **All the DAO SQL statements** in `AppointmentDAOImpl`, `PatientDAO`, `BillDAO`
5. **`AppointmentManager`** — orchestrate the DAOs to fulfil each use case
6. **`RegisterAppointmentServlet`** — wire up the parameters
7. Restyle `style.css` and improve `app.js` UX (currently shows raw JSON)

## Before you run this
1. Set up MySQL and run `database/schema.sql`.
2. Set your DB credentials as environment variables — **do not** put them
   in `DBConnection.java`, since this repo is pushed to a *public* GitHub
   repo for Task D and anything hardcoded there would be exposed:
   ```
   export DB_URL="jdbc:mysql://localhost:3306/dental_clinic?useSSL=false&serverTimezone=UTC"
   export DB_USER="root"
   export DB_PASSWORD="your-local-mysql-password"
   ```
   If you're running Tomcat from an IDE, set these in its run configuration
   instead (IntelliJ: Run Config → Environment variables; Eclipse: Run
   Configurations → Environment tab). `DB_URL`/`DB_USER` fall back to
   sensible local defaults if unset, but `DB_PASSWORD` is required — the
   app throws a clear error on startup if it's missing, rather than
   silently trying a blank password.
3. Check your Tomcat version:
   - **Tomcat 10+** → the `jakarta.servlet.*` imports already in this project are correct.
   - **Tomcat 9 or older** → change every `jakarta.servlet.*` import to `javax.servlet.*`
     in the servlet classes, and change `web.xml`'s version to `"4.0"`.
4. `mvn clean package` to build the WAR, then deploy it to Tomcat's `webapps/` folder
   (or run it directly from your IDE if it has Tomcat integration, e.g. Eclipse
   with the "Servers" view, or IntelliJ Ultimate).

## Suggested build order
1. Get `login()` working first (even a hardcoded check) so you can see the
   app-section appear.
2. Implement `PatientDAO.save()` and `AppointmentDAOImpl.save()`, then wire
   up `registerAppointment()` — test with the Register tab.
3. Implement `findByApptNumber()` and `searchAppointment()` — test with Search.
4. Implement `cancel()` and `cancelAppointment()`.
5. Implement `calculateTotal()`, `BillDAO`, and `generateBill()`.

Good luck — come back with any class/method you're stuck on and I'll help
you debug it.
