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

### Files Added (New)
- `src/main/java/com/kartik/hrms/entity/AuthToken.java`
- `src/main/java/com/kartik/hrms/repository/AuthTokenRepository.java`
- `src/main/java/com/kartik/hrms/security/AuthenticatedUser.java`
- `src/main/java/com/kartik/hrms/security/AuthTokenFilter.java`
- `src/main/java/com/kartik/hrms/security/CryptoService.java`
- `src/main/java/com/kartik/hrms/security/TokenService.java`
- `src/main/java/com/kartik/hrms/controller/EmployeeController.java`
- `src/main/java/com/kartik/hrms/dto/LoginRequestDTO.java`
- `src/main/java/com/kartik/hrms/dto/LoginResponseDTO.java`
- `src/main/java/com/kartik/hrms/dto/EmployeeRequestDTO.java`
- `src/main/java/com/kartik/hrms/dto/EmployeeResponseDTO.java`
- `src/main/resources/db/migration/V1__employee_required_fields.sql`
- `src/main/resources/db/migration/V2__employee_profile_and_owner_fk_update.sql`

### Files Updated (Existing)
- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/java/com/kartik/hrms/config/SecurityConfig.java`
- `src/main/java/com/kartik/hrms/entity/User.java`
- `src/main/java/com/kartik/hrms/entity/Employee.java`
- `src/main/java/com/kartik/hrms/repository/UserRepository.java`
- `src/main/java/com/kartik/hrms/service/UserService.java`
- `src/main/java/com/kartik/hrms/service/EmployeeService.java`
- `src/main/java/com/kartik/hrms/controller/UserController.java`
- `src/main/java/com/kartik/hrms/dto/UserResponseDTO.java`
