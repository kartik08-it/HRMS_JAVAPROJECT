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
