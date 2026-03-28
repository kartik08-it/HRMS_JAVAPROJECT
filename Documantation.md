# HRMS Backend (Spring Boot + MySQL)

## 📌 Project Overview
This is the backend for the HRMS (Human Resource Management System) built using:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL
- Maven

---

## 🛠 Prerequisites

Make sure the following are installed:

### 1️⃣ Java 21
Check:
```bash
java -version
```

### 2️⃣ MySQL
Check:
```bash
mysql --version
```

### 3️⃣ Maven (Optional if using wrapper)
We are using Maven Wrapper (`mvnw`), so no need to install Maven globally.

---

## 🗄 Database Setup

Login to MySQL:

```bash
sudo mysql
```

Create database:

```sql
CREATE DATABASE hrms_system;
```

Exit:

```sql
exit;
```

---

## ⚙ Application Configuration

Open:

```
src/main/resources/application.properties
```

Make sure database configuration matches:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrms_system
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8080
```

---

## ▶ Running The Project

### Clean the project
```bash
./mvnw clean
```

### Run the application
```bash
./mvnw spring-boot:run
```

If started successfully, you should see:

```
Tomcat started on port 8080
Started HrmsApplication
```

---

## 🌐 Access Application

Open in browser:

```
http://localhost:8080
```

---

## 🛑 If Port 8080 Is Already In Use

Check which process is using it:

```bash
lsof -i :8080
```

Kill the process:

```bash
kill -9 <PID>
```

---

## 🔐 Default Spring Security Login (Temporary)

Username:
```
user
```

Password:
Check console output for:
```
Using generated security password: XXXXX
```

---

## 📦 Build JAR File

```bash
./mvnw clean package
```

The jar file will be inside:
```
target/
```

Run jar:

```bash
java -jar target/hrms-0.0.1-SNAPSHOT.jar
```

---

## 🧠 Notes

- Embedded Tomcat is included in Spring Boot (no need to install separately).
- Database must exist before running.
- Java 21 is required.

---

## 📊 Attendance Dashboard API

Endpoint:
```
GET /api/attendance/dashboard?date=YYYY-MM-DD
```

Notes:
- `date` is optional. If omitted, the server uses the current date.
- Requires authenticated admin access.
- Returns summary, department breakdown, recent activity, late arrivals, absentees, and weekly trend.

### Sample Data
- `src/main/resources/data.sql` seeds sample attendance data on startup.
- It only inserts sample employees/attendance if at least one `ADMIN` user exists.
- After creating an admin via `/api/users/bootstrap-admin`, restart the app to load the sample data.

# Project Learning Curve :- 
1. entity
2. repository
3. service
4. controller
5. dto 
6. config
7. exception


---

## 9. Today Implementation Log (Appended)

### Functionalities Implemented
- Implemented token-based authentication with login/logout and Bearer token validation middleware.
- Added persistent token storage and token revocation support.
- Enforced stateless Spring Security configuration for API requests.
- Added encryption for sensitive user fields (email, phone) and hashing for password.
- Added hash-based uniqueness checks for encrypted email/phone.
- Implemented user CRUD with security checks and role normalization.
- Introduced admin-first model:
  - bootstrap admin creation endpoint
  - admin-only access for `/api/users/**`
- Split domain responsibilities:
  - `users` table for admin login/access control
  - `employees` table for employee profiles
- Updated employee module with profile segmentation (`HR` / `EMPLOYEE`).
- Added employee CRUD endpoints and service logic with admin authorization checks.
- Introduced Flyway migration-based schema management.
- Added migration files for employee table constraints and ownership/profile updates.


# 15 march 2026
issue while login 
  - user try to login
  - system check user
   SELECT * FROM users WHERE email_hash=?
   - then system check the auth tokens
   SELECT * FROM auth_tokens WHERE expires_at < NOW()
   - then system try to delete that token 
   authTokenRepository.delete(token)
   - but because method is not  in transaction, hibernate throws error

  
  # 16 march 2026
  Client (React / Postman)
        ↓
  EmployeeController
          ↓
  EmployeeService
          ↓
  EmployeeRepository
          ↓
  Database
  

# 17 march 2026
debugged the migration issue while appending some columns in employees,
currentlly for migration i have to run this much query to migrate the db 


./mvnw -q -DskipTests \
  -Dflyway.url=jdbc:mysql://localhost:3306/hrms_system \
  -Dflyway.user=admin \
  -Dflyway.password=password \
  flyway:migrate
