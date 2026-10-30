# Smart Student Management System

A web application for managing student records and monitoring attendance (42 classes, 75% minimum).
Built with **Java 17, Spring Boot 3, Spring Security, Jackson JSON storage, Bootstrap 5**, tested with **JUnit 5**
and delivered through **Maven, Jenkins and Docker**. No SQL or external database is used.

## 1. Features

| Area | What it does |
|------|--------------|
| Authentication | Login with username **or** registration number, BCrypt-hashed passwords, sessions, logout, role-based redirect (ADMIN / STUDENT) |
| Admin | Dashboard cards (calculated live), add / view / edit / delete students, search by name or registration number, filter by department and semester, student profiles, attendance monitoring, shortage list |
| Student | Personal dashboard: profile, large attendance card, progress bar with 75% marker, classes missed, additional classes needed, eligibility message |
| Attendance | `[ - ]  Classes Attended  [ + ]` buttons, range 0-42, buttons disabled at the limits, every change saved to JSON and logged in history |
| Storage | `data/*.json`, auto-created, synchronized atomic writes, corrupted files backed up and recovered |
| DevOps | Maven build, JUnit 5 (27 tests), Jenkinsfile, Dockerfile, docker-compose with a persistent volume |

**Attendance rules:** `Attendance % = (attended / 42) x 100`. Minimum = 75% = **32 classes**.
30 -> 71.43% shortage, 31 -> 73.81% shortage, 32 -> 76.19% eligible, 35 -> 83.33%, 42 -> 100%.

> The + / - buttons are **simulated attendance adjustment controls for demonstration only**. They are not verified classroom attendance marking, and the UI says so.

## 2. Demo credentials

| Role | Username / Registration No. | Password | Attendance (initial) |
|------|-----------------------------|----------|----------------------|
| ADMIN | `admin` | `Admin@123` | - |
| STUDENT | `22BCE1001` (Aarav Sharma) | `Student@123` | 30/42 - shortage |
| STUDENT | `22BCE1002` (Priya Nair) | `Student@123` | 35/42 - eligible |
| STUDENT | `22BEC2001` (Rohan Mehta) | `Student@123` | 31/42 - shortage |
| STUDENT | `22BME3001` (Ananya Iyer) | `Student@123` | 42/42 - eligible |
| STUDENT | `22BIT4001` (Karthik Reddy) | `Student@123` | 32/42 - eligible (boundary) |
| STUDENT | `22BIT4002` (Sneha Kapoor) | `Student@123` | 20/42 - shortage |

Students may also sign in with the lowercase username (e.g. `22bce1001`). Demo data is created only when `data/users.json` is empty.
New students added by the admin get the password `Student@123` unless the admin types another one.

## 3. Technology stack

Java 17 (runs on newer JDKs too, e.g. 25), Spring Boot 3.5, Spring Web, Spring Security, Jackson, HTML5/CSS3/JavaScript, Bootstrap 5 + Bootstrap Icons (loaded from the jsDelivr CDN, so an internet connection is needed in the browser), Maven, JUnit 5, Git/GitHub, Jenkins, Docker, Docker Compose.

## 4. Project structure

```
SmartStudentManagement/
├── pom.xml                      Maven build (Spring Boot parent, executable JAR)
├── Jenkinsfile                  Declarative CI/CD pipeline
├── Dockerfile / docker-compose.yml / .dockerignore
├── data/                        JSON storage (created at runtime)
│   ├── students.json  users.json  attendance.json  attendance_history.json
└── src/
    ├── main/java/com/vaishnavi/sms/
    │   ├── SmartStudentApplication.java
    │   ├── config/        SecurityConfig, DataInitializer
    │   ├── controller/    AuthController, StudentController, AttendanceController, AdminController
    │   ├── service/       AuthService, StudentService, AttendanceService
    │   ├── repository/    StudentRepository, UserRepository, AttendanceRepository, JsonFileStore
    │   ├── model/         Student, User, Attendance, AttendanceHistory
    │   ├── dto/           AttendanceSummary, StudentView, StudentRequest
    │   └── exception/     ApiExceptions, GlobalExceptionHandler
    ├── main/resources/
    │   ├── application.properties
    │   └── static/        login.html, css/app.css, js/app.js,
    │                      admin/{dashboard,students,student-form,student-profile,attendance}.html,
    │                      student/dashboard.html
    └── test/java/com/vaishnavi/sms/   StudentServiceTest, AttendanceServiceTest, AuthServiceTest, SecurityIntegrationTest
```

Pages: Login, Admin Dashboard, Student Dashboard, Student Management, **Add Student** (`student-form.html`), **Edit Student** (`student-form.html?id=STU001`), Student Profile, Attendance Monitoring.

## 5. Prerequisites and installation (Windows)

| Tool | Needed for | Download / install |
|------|------------|--------------------|
| JDK 17+ | build and run | https://adoptium.net/temurin/releases/?version=17 or `winget install EclipseAdoptium.Temurin.17.JDK` |
| Maven 3.9+ | build and test | https://maven.apache.org/download.cgi - unzip, add `bin` to PATH. Check: `mvn -version` |
| Git | source control, Jenkins checkout | https://git-scm.com/download/win or `winget install Git.Git` |
| Docker Desktop with Docker Compose v2.17+ | containers and Jenkins deployment health checks | https://www.docker.com/products/docker-desktop/ (enable WSL 2, restart) |
| Jenkins | CI/CD | https://www.jenkins.io/download/ (Windows `.msi`) |

Maven manual setup (PowerShell, current session):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"   # your JDK folder
$env:Path = "C:\tools\apache-maven-3.9.9\bin;$env:JAVA_HOME\bin;$env:Path"
java -version ; mvn -version
```

## 6. Run locally

```powershell
cd SmartStudentManagement
mvn clean package            # compiles, runs 27 tests, builds target/smart-student-management-1.0.0.jar
java -jar target/smart-student-management-1.0.0.jar
```

or during development: `mvn spring-boot:run`.

Open **http://localhost:8080** (redirects to the login page). Use another port with `$env:PORT="8090"` before starting.
Data is written to `./data` (change with `$env:APP_DATA_DIR="D:\sms-data"`).

## 7. How JSON storage works

* Four files in `data/`: `students.json`, `users.json`, `attendance.json`, `attendance_history.json`.
* `JsonFileStore` (Jackson `ObjectMapper`) creates missing files as `[]`, reads the whole array and writes it back.
* Writes are `synchronized`, go to a temp file first and are then moved over the real file, so a crash cannot leave a half-written file.
* Empty files are treated as empty lists. A corrupted file is renamed to `<name>.json.corrupt-<timestamp>` and a fresh empty file is created; the app keeps running.
* Every student add/edit/delete and every attendance change is saved immediately, so data survives restarts.
* Dashboard numbers are computed from these files on each request; nothing is hardcoded.
* To reset the demo: stop the app, delete the `data` folder, start again (demo data is re-created).

## 8. REST API (summary)

| Method & path | Role | Purpose |
|---------------|------|---------|
| `POST /login`, `GET /logout` | all | Sign in / out |
| `GET /api/auth/me` | any user | Current user |
| `GET /api/students?q=&department=&semester=&status=` | ADMIN | Search / filter |
| `GET/POST/PUT/DELETE /api/students[/{id}]` | ADMIN | CRUD |
| `GET /api/admin/stats` | ADMIN | Dashboard figures |
| `GET /api/attendance`, `/shortage`, `/{id}/history` | ADMIN | Monitoring |
| `GET /api/me/student`, `/api/me/attendance`, `/history` | STUDENT | Own data only |
| `POST /api/me/attendance/increase` / `decrease` | STUDENT | +1 / -1 (400 when out of range) |

Status codes: 200, 201, 204, 400 (validation / range), 401 (not signed in), 403 (wrong role), 404, 409 (duplicate registration number or username).

## 9. Run the JUnit tests

```powershell
mvn test
```

Covers student add/search/update/delete, duplicate and required-field validation, percentage calculation, increase/decrease, bounds 0 and 42, eligibility at 32 and shortage at 31, persistence across restart, corrupted-file recovery, admin/student login, invalid password, hashed passwords, and unauthorized access (401/403) through the real security configuration. Reports: `target/surefire-reports`.

## 10. Docker

```powershell
mvn clean package                                   # the Dockerfile copies this JAR
docker build -t smart-student-management:1.0.0 .
docker compose up -d                                # http://localhost:8080
docker compose logs -f
docker compose down                                 # keeps data
docker compose down -v                              # also deletes the data volume
```

JSON files live in the named volume `student-data` mounted at `/app/data`, so data survives `docker compose restart` and container re-creation. Environment variables: `PORT`, `APP_DATA_DIR`, `JAVA_OPTS`, `APP_SEED_DEMO_DATA`.

## 11. Jenkins CI/CD

Pipeline stages (see `Jenkinsfile`): **Checkout -> Build (`mvn clean compile`) -> Test (`mvn test`, JUnit report published) -> Package (executable JAR) -> Docker Build -> Release (tag 1.0.0 and latest) -> Deployment (`docker compose up -d --wait`)**. Deployment waits for the Compose health check; a compile, test, or health-check failure marks the pipeline failed.

Setup:

1. Install Jenkins and open http://localhost:8080 (if the app already uses 8080, start Jenkins on another port, e.g. `--httpPort=9090`, or run the app on another port).
2. Install the suggested plugins (Pipeline, Git, JUnit).
3. **Manage Jenkins -> Tools**: add a JDK named **`JDK17`** and a Maven named **`Maven3`** (names must match the Jenkinsfile).
4. Docker Desktop must be running and the Jenkins service account must be able to run `docker` (on Windows, run the Jenkins service as your user or add it to the `docker-users` group, then restart the service).
5. Push the project to GitHub:
   ```powershell
   git init ; git add . ; git commit -m "Smart Student Management System"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
6. **New Item -> Pipeline** -> *Pipeline script from SCM* -> Git -> repository URL (+ credentials ID from the Jenkins credentials store for a private repo) -> branch `*/main` -> Script Path `Jenkinsfile` -> Save -> **Build Now**.
7. Check **Test Result** for the JUnit report and the console log for the success message. Passwords or tokens are never stored in the repository; use Jenkins credentials.

## 12. Common errors and solutions

| Problem | Solution |
|---------|----------|
| `Port 8080 was already in use` | Stop the other program (`netstat -ano \| findstr :8080`) or run with `$env:PORT="8090"` |
| `mvn` is not recognized | Add Maven's `bin` folder to PATH and open a new terminal |
| `release version 17 not supported` | Use JDK 17 or newer; check `java -version` and `JAVA_HOME` |
| Login page loads without styles | Browser needs internet for the Bootstrap CDN |
| `docker: error during connect` | Start Docker Desktop and wait until it says *running* |
| Dockerfile: `target/...jar not found` | Run `mvn clean package` first |
| Data looks empty or reset | Check `APP_DATA_DIR`; in Docker don't use `docker compose down -v` |
| Jenkins: `Tool type "jdk" does not have an install of "JDK17"` | Configure tools with the exact names `JDK17` and `Maven3` |
| Jenkins cannot run `docker` | Ensure Docker Desktop is running and the Jenkins service user has Docker access |
| Corrupted JSON | Automatically backed up as `*.corrupt-<time>`; fix the backup or let the app start fresh |

## 13. Faculty demonstration checklist

- [ ] Show the structure: controller / service / repository / model / config, and the four JSON files in `data/`.
- [ ] `mvn clean package`: 27 JUnit tests pass, JAR is created.
- [ ] Start the app and open http://localhost:8080.
- [ ] Log in as `admin`: cards show students, 42 classes, eligible, shortage (calculated from JSON).
- [ ] Add a student, search it, filter by department/semester, edit, view profile, delete.
- [ ] Try a duplicate registration number (error shown) and an empty form (field errors shown).
- [ ] Open Attendance Monitoring: percentages, status badges, "Below 75%" filter.
- [ ] Log out, log in as `22BIT4001` (32/42): eligible. Press **-** once: 31/42 = 73.81%, red, needs 1 more class. Press **+**: green again.
- [ ] Go to 0 and 42: the - / + button is disabled.
- [ ] Keep the admin page open in another window: it refreshes automatically (every 5 s) after the student's change; open the profile to see the history.
- [ ] Show `data/attendance.json` and `attendance_history.json` updated; stop and restart the app: values persist.
- [ ] Try `/admin/dashboard.html` as a student (redirected) and a wrong password (error).
- [ ] `docker compose up -d`, show the app, `docker compose restart`, data still there.
- [ ] Show the Jenkins pipeline run: all stages green and the JUnit test report.
