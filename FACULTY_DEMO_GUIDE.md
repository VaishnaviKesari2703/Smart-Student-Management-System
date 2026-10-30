# 🎓 Smart Student Management System — Faculty Demo Guide
**Vaishnavi Kesari | Spring Boot · JSON · Maven · JUnit 5 · Jenkins CI/CD · Docker · GitHub**

---

## 📋 Table of Contents
1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [How to Run the Application](#4-how-to-run-the-application)
5. [Demo Credentials](#5-demo-credentials)
6. [Maven Build System](#6-maven-build-system)
7. [JUnit 5 Testing](#7-junit-5-testing)
8. [Docker Containerisation](#8-docker-containerisation)
9. [Jenkins CI/CD Pipeline](#9-jenkins-cicd-pipeline)
10. [GitHub & Release Tag](#10-github--release-tag)
11. [Faculty Demonstration Script](#11-faculty-demonstration-script)
12. [Architecture Diagrams](#12-architecture-diagrams)

---

## 1. Project Overview

The **Smart Student Management System (SMS)** is a full-stack Java web application that demonstrates:

| Capability | Implementation |
|---|---|
| Student registration and management | ADMIN role creates/edits/deletes students |
| Attendance tracking | ADMIN marks Present/Absent; percentages calculated |
| Secure login | Spring Security with role-based access control |
| Persistent storage | JSON files (no database required) |
| Automated build | Apache Maven (`mvn clean package`) |
| Automated testing | JUnit 5 (27 tests across 5 test classes) |
| Containerisation | Docker + Docker Compose |
| CI/CD pipeline | Jenkins 7-stage declarative pipeline |
| Version control | GitHub with semantic versioning tag `v1.0.0` |

**GitHub Repository:** https://github.com/VaishnaviKesari2703/Smart-Student-Management-System

---

## 2. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.5.6 |
| Security | Spring Security | 6.x |
| JSON Processing | Jackson | (bundled with Spring Boot) |
| Build Tool | Apache Maven | 3.9.x |
| Testing | JUnit 5 + Mockito | JUnit 5.11.x |
| Frontend | HTML5, CSS3, JavaScript (Vanilla) | — |
| Containerisation | Docker + Docker Compose | Docker 28.x / Compose v2 |
| CI/CD | Jenkins | 2.573 |
| Version Control | Git + GitHub | — |

---

## 3. Project Structure

```
SmartStudentManagement/
├── pom.xml                          ← Maven build configuration
├── Dockerfile                       ← Docker image definition
├── docker-compose.yml               ← Container orchestration
├── Jenkinsfile                      ← CI/CD pipeline definition
├── README.md                        ← Project documentation
├── FACULTY_DEMO_GUIDE.md            ← This file
├── data/                            ← JSON persistent storage (auto-created)
│   ├── students.json
│   ├── attendance.json
│   └── users.json
└── src/
    ├── main/
    │   ├── java/com/vaishnavi/sms/
    │   │   ├── SmartStudentApplication.java     ← Entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java           ← Role-based auth config
    │   │   │   └── DataInitializer.java          ← Seeds default users
    │   │   ├── controller/
    │   │   │   ├── AdminController.java          ← Admin REST endpoints
    │   │   │   ├── StudentController.java        ← Student REST endpoints
    │   │   │   ├── AttendanceController.java     ← Attendance REST endpoints
    │   │   │   └── AuthController.java           ← Login/logout endpoints
    │   │   ├── service/
    │   │   │   ├── StudentService.java           ← Student business logic
    │   │   │   ├── AttendanceService.java        ← Attendance calculations
    │   │   │   └── AuthService.java              ← Authentication logic
    │   │   ├── repository/
    │   │   │   ├── JsonFileStore.java            ← Generic JSON read/write
    │   │   │   ├── StudentRepository.java        ← Student data access
    │   │   │   ├── AttendanceRepository.java     ← Attendance data access
    │   │   │   └── UserRepository.java           ← User data access
    │   │   ├── model/
    │   │   │   ├── Student.java
    │   │   │   ├── Attendance.java
    │   │   │   ├── AttendanceHistory.java
    │   │   │   └── User.java
    │   │   ├── dto/
    │   │   │   ├── StudentRequest.java
    │   │   │   ├── StudentView.java
    │   │   │   └── AttendanceSummary.java
    │   │   └── exception/
    │   │       ├── ApiExceptions.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── login.html
    │           ├── admin/
    │           │   ├── dashboard.html
    │           │   ├── students.html
    │           │   ├── student-form.html
    │           │   ├── student-profile.html
    │           │   └── attendance.html
    │           ├── student/
    │           │   └── dashboard.html
    │           ├── css/app.css
    │           └── js/app.js
    └── test/
        └── java/com/vaishnavi/sms/
            ├── StudentServiceTest.java          ← 8 student tests
            ├── AttendanceServiceTest.java       ← 7 attendance tests
            ├── AuthServiceTest.java             ← 6 auth tests
            ├── SecurityIntegrationTest.java     ← 4 security tests
            └── TestContext.java                 ← Shared test setup
```

---

## 4. How to Run the Application

### Option A — Run Directly with Maven (Fastest for Demo)

```powershell
# Open PowerShell, navigate to project
cd C:\Users\vaish\.gemini\antigravity-ide\scratch\SmartStudentManagement

# Run the Spring Boot application
mvn spring-boot:run
```
Then open your browser at: **http://localhost:8081**

### Option B — Build JAR and Run

```powershell
# Build executable JAR (runs all 27 tests automatically)
mvn clean package

# Run the packaged JAR
java -jar target/smart-student-management-1.0.0.jar
```

### Option C — Run with Docker Compose (Production-like)

```powershell
# Start containerised application
docker compose up -d

# View logs
docker compose logs -f

# Stop
docker compose down
```
Then open: **http://localhost:8081**

---

## 5. Demo Credentials

| Role | Username | Password | Access |
|---|---|---|---|
| Administrator | `admin` | `admin123` | Full access: manage students, mark attendance, view all reports |
| Student | `student1` | `pass123` | View own profile, attendance percentage |

> **Note:** These are seeded automatically by `DataInitializer.java` on first startup if `data/users.json` doesn't exist.

---

## 6. Maven Build System

### What Maven Does
Maven is the **automated build tool** for this project. The `pom.xml` file defines:
- Project metadata (groupId, artifactId, version)
- All library dependencies (Spring Boot, Jackson, JUnit 5)
- Build plugins (spring-boot-maven-plugin)
- Build lifecycle phases

### Key Maven Commands

```powershell
# Compile Java source code
mvn compile

# Run all 27 JUnit tests
mvn test

# Package into executable JAR (skips re-running tests)
mvn package -DskipTests

# Full clean build + test + package (CI/CD standard)
mvn clean package

# Run the app via Maven plugin
mvn spring-boot:run
```

### pom.xml Highlights (show during demo)

```xml
<groupId>com.vaishnavi</groupId>
<artifactId>smart-student-management</artifactId>
<version>1.0.0</version>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Output Artifact
After `mvn package`, the file `target/smart-student-management-1.0.0.jar` is a **self-contained executable JAR** — it includes the embedded Tomcat server and all dependencies. No separate server installation needed.

---

## 7. JUnit 5 Testing

### Test Summary

| Test Class | Tests | What It Tests |
|---|---|---|
| `StudentServiceTest` | 8 | Create, find, update, delete student; validation errors |
| `AttendanceServiceTest` | 7 | Mark attendance, calculate percentage, fetch history |
| `AuthServiceTest` | 6 | Login success/failure, role assignment, password hashing |
| `SecurityIntegrationTest` | 4 | Protected endpoints reject unauthenticated access; role enforcement |
| **Total** | **25** | **Full service and security layer coverage** |

### Run Tests

```powershell
# Run all tests with full output
mvn test

# Run a single test class
mvn test -Dtest=StudentServiceTest

# Generate HTML test report
mvn surefire-report:report
# Report: target/site/surefire-report.html
```

### Sample Test Code (show faculty `StudentServiceTest.java`)

```java
@Test
void createStudent_withValidData_savesSuccessfully() {
    StudentRequest req = new StudentRequest("Alice", "CS", 1);
    Student saved = studentService.createStudent(req);
    assertNotNull(saved.getId());
    assertEquals("Alice", saved.getName());
}

@Test
void createStudent_withDuplicateName_throwsException() {
    studentService.createStudent(new StudentRequest("Bob", "IT", 2));
    assertThrows(StudentAlreadyExistsException.class,
        () -> studentService.createStudent(new StudentRequest("Bob", "IT", 2)));
}
```

### What Makes a Good Test?
- **Arrange** — set up test data
- **Act** — call the method being tested
- **Assert** — verify the result is correct

---

## 8. Docker Containerisation

### What Docker Does
Docker packages the application and all its dependencies (Java runtime, configuration) into a **portable, isolated container** that runs identically on any machine.

### Dockerfile Explanation

```dockerfile
# Step 1: Use official OpenJDK 17 base image
FROM eclipse-temurin:17-jre-alpine

# Step 2: Set working directory inside container
WORKDIR /app

# Step 3: Copy the built JAR into the container
COPY target/smart-student-management-1.0.0.jar app.jar

# Step 4: Expose port 8081
EXPOSE 8081

# Step 5: Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Commands

```powershell
# Build the Docker image
docker build -t smart-student-management:1.0.0 .

# List available images
docker image ls smart-student-management

# Run container manually
docker run -p 8081:8081 smart-student-management:1.0.0

# Start with Docker Compose (includes volume for data persistence)
docker compose up -d

# See running containers
docker compose ps

# Stop and remove containers
docker compose down
```

### docker-compose.yml Explanation

```yaml
services:
  app:
    image: smart-student-management:1.0.0
    ports:
      - "8081:8081"          # host:container port mapping
    volumes:
      - student-data:/app/data  # persists JSON data between restarts

volumes:
  student-data:              # named volume (survives container restarts)
```

### Key Docker Concepts (for faculty explanation)
- **Image** = blueprint (read-only snapshot of the app)
- **Container** = running instance of an image
- **Volume** = persistent storage that survives container restarts
- **Port mapping** = `8081:8081` means "host port 8081 → container port 8081"

---

## 9. Jenkins CI/CD Pipeline

### What Jenkins Does
Jenkins is a **Continuous Integration / Continuous Deployment (CI/CD)** server. Every time code is pushed to GitHub, Jenkins automatically:
1. **Checks out** the latest code from GitHub
2. **Compiles** it with Maven
3. **Runs** all 27 JUnit tests
4. **Packages** it into a JAR
5. **Builds** a Docker image
6. **Tags** the image as a release
7. **Deploys** it with Docker Compose

### Pipeline Stages (Jenkinsfile)

```
Checkout → Build → Test → Package → Docker Build → Release → Deployment
```

| Stage | Command | Purpose |
|---|---|---|
| **Checkout** | `git checkout` | Pull latest code from GitHub |
| **Build** | `mvn -B clean compile` | Compile Java source |
| **Test** | `mvn -B test` | Run all JUnit 5 tests |
| **Package** | `mvn -B package -DskipTests` | Create executable JAR |
| **Docker Build** | `docker build -t smart-student-management:1.0.0 .` | Build container image |
| **Release** | `docker tag ... :latest` | Tag as latest release |
| **Deployment** | `docker compose up -d` | Deploy the new container |

### How to Run the Pipeline (Jenkins UI)

1. **Open Jenkins:** http://localhost:8080
2. **Log in** with your Jenkins admin credentials
3. **Create a Pipeline Job:**
   - Click **"New Item"** → Enter name `SmartStudentManagement` → Select **"Pipeline"**
   - Under **Pipeline** section → set Definition to **"Pipeline script from SCM"**
   - SCM: **Git**, Repository URL: `https://github.com/VaishnaviKesari2703/Smart-Student-Management-System.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
   - Click **Save**
4. Click **"Build Now"**
5. Watch the **Stage View** — each stage turns green on success

### Jenkins Prerequisites (one-time setup)

In **Manage Jenkins → Tools**, configure:
- **JDK**: Name = `JDK17`, JAVA_HOME = `C:\Program Files\Java\jdk-17`
- **Maven**: Name = `Maven3`, MAVEN_HOME = `C:\Program Files\Apache\maven` (or wherever Maven is installed)

### Jenkinsfile Key Design Decisions
- **`agent any`** — runs on any available Jenkins node
- **`disableConcurrentBuilds()`** — prevents two builds running simultaneously
- **`buildDiscarder(logRotator(numToKeepStr: '10'))`** — keeps only last 10 build logs
- **`timeout(time: 30, unit: 'MINUTES')`** — auto-fails if a build hangs
- **`runCmd()` helper** — automatically uses `sh` on Linux or `bat` on Windows

---

## 10. GitHub & Release Tag

### Repository
- **URL:** https://github.com/VaishnaviKesari2703/Smart-Student-Management-System
- **Branch:** `main`
- **Release Tag:** `v1.0.0`

### Git Commands Used

```powershell
# Clone the project
git clone https://github.com/VaishnaviKesari2703/Smart-Student-Management-System.git

# View all commits
git log --oneline

# View release tags
git tag -l

# Checkout the v1.0.0 release
git checkout v1.0.0
```

### Release Tag Meaning
The tag `v1.0.0` marks the **exact commit** that represents the production release. This is standard **Semantic Versioning (SemVer)**:
- `1` = Major version (breaking changes would increment this)
- `0` = Minor version (new features)
- `0` = Patch version (bug fixes)

On GitHub, go to **Releases** tab → you will see `v1.0.0` listed as a release.

---

## 11. Faculty Demonstration Script

### Step 1 — Show the GitHub Repository (2 min)
1. Open: https://github.com/VaishnaviKesari2703/Smart-Student-Management-System
2. Show the **file structure** — explain `src/`, `Dockerfile`, `Jenkinsfile`, `pom.xml`
3. Click **"Releases"** → show `v1.0.0` tag with commit message
4. Click **"Commits"** → show commit history

### Step 2 — Show the Maven Build (2 min)
```powershell
cd C:\Users\vaish\.gemini\antigravity-ide\scratch\SmartStudentManagement
mvn clean package
```
Point out: "This compiles the code, runs 27 tests, and packages the JAR automatically."

### Step 3 — Show JUnit Test Results (1 min)
```powershell
mvn test
```
Scroll through output — show "Tests run: 27, Failures: 0, Errors: 0"

### Step 4 — Show Docker Image (1 min)
```powershell
docker image ls smart-student-management
```
Show the image `smart-student-management:1.0.0` and `latest` both listed.

### Step 5 — Start Application with Docker Compose (1 min)
```powershell
docker compose up -d
docker compose ps
```
Show container status = "running"

### Step 6 — Live Application Demo (3 min)
1. Open browser → http://localhost:8081
2. **Login as admin** (`admin` / `admin123`)
3. Show **Admin Dashboard** → Student count, attendance stats
4. Navigate to **Students** → add a new student
5. Navigate to **Attendance** → mark attendance for today
6. **Logout**, then **login as student** (`student1` / `pass123`)
7. Show student's own **attendance dashboard** with percentage

### Step 7 — Show Jenkinsfile (2 min)
Open `Jenkinsfile` in editor or on GitHub — walk through each stage:
```
Checkout → Build → Test → Package → Docker Build → Release → Deployment
```
Explain: "Every push to GitHub triggers this entire pipeline automatically."

### Step 8 — Show Jenkins (2 min)
1. Open: http://localhost:8080
2. Navigate to the `SmartStudentManagement` pipeline job
3. Show **Stage View** with all green stages
4. Click a build → **Console Output** → show test results being collected

---

## 12. Architecture Diagrams

### Application Architecture

```
Browser (HTML/JS)
       │
       ▼
Spring Boot (Port 8081)
       │
       ├── Spring Security ──── Role Check (ADMIN / STUDENT)
       │
       ├── Controllers ─────────── REST API endpoints
       │       ├── AdminController
       │       ├── StudentController
       │       ├── AttendanceController
       │       └── AuthController
       │
       ├── Services ────────────── Business logic
       │       ├── StudentService
       │       ├── AttendanceService
       │       └── AuthService
       │
       └── Repositories ────────── JSON file I/O (Jackson)
               ├── StudentRepository    → data/students.json
               ├── AttendanceRepository → data/attendance.json
               └── UserRepository       → data/users.json
```

### CI/CD Pipeline Architecture

```
Developer
   │
   │ git push
   ▼
GitHub (main branch)
   │
   │ webhook / manual trigger
   ▼
Jenkins Pipeline
   │
   ├─ Stage 1: Checkout ──── git clone from GitHub
   ├─ Stage 2: Build ──────── mvn clean compile
   ├─ Stage 3: Test ───────── mvn test (27 JUnit tests)
   ├─ Stage 4: Package ─────── mvn package → .jar file
   ├─ Stage 5: Docker Build ── docker build → image
   ├─ Stage 6: Release ─────── docker tag → :1.0.0 + :latest
   └─ Stage 7: Deployment ──── docker compose up -d
                                        │
                                        ▼
                               Running Container
                               (http://localhost:8081)
```

### Data Flow (JSON Storage)

```
HTTP Request
     │
     ▼
Controller (validates input)
     │
     ▼
Service (business logic)
     │
     ▼
Repository (read/write JSON file)
     │
     ▼
Jackson ObjectMapper
     │
     ▼
data/students.json  ←──── Persistent storage on disk
```

---

## 📌 Quick Reference Commands

```powershell
# --- Maven ---
mvn clean package          # Build + test
mvn test                   # Test only
mvn spring-boot:run        # Run locally

# --- Docker ---
docker build -t smart-student-management:1.0.0 .
docker image ls smart-student-management
docker compose up -d       # Start
docker compose down        # Stop
docker compose logs -f     # Live logs

# --- Git ---
git log --oneline          # Commit history
git tag -l                 # List tags (shows v1.0.0)
git status                 # Working tree status

# --- App URLs ---
# Application:  http://localhost:8081
# Jenkins:      http://localhost:8080
# GitHub:       https://github.com/VaishnaviKesari2703/Smart-Student-Management-System
```

---

*Guide generated for faculty demonstration — Smart Student Management System v1.0.0*
