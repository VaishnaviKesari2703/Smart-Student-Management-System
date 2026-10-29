# Smart Student Management System

> A console-based Java application demonstrating CRUD operations, JUnit 5 testing,
> Maven build automation, Docker containerisation, and Jenkins CI/CD.

---

## 📋 Project Overview

This project was developed as a college submission to demonstrate end-to-end software
engineering practices within a three-hour window:

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build | Apache Maven 3.9.x |
| Testing | JUnit 5 (Jupiter) |
| Version Control | Git + GitHub |
| CI/CD | Jenkins (Declarative Pipeline) |
| Containerisation | Docker (Eclipse Temurin 17 JRE) |

> ⚠️ **Data is in-memory only.** All student records are lost when the application exits.

---

## 🏗️ Architecture

```
┌──────────────┐   stdin   ┌──────────────────┐   CRUD   ┌─────────────────────┐
│   Main.java  │ ────────► │ StudentService.java│ ───────► │ LinkedHashMap<>     │
│ (Console UI) │           │  (Business Logic) │          │ (In-memory Storage) │
└──────────────┘           └──────────────────┘          └─────────────────────┘
                                     │
                                     ▼
                           ┌──────────────────┐
                           │   Student.java    │
                           │  (Model + Grade) │
                           └──────────────────┘
```

### CI/CD Workflow

```
GitHub Push ──► Jenkins Pipeline ──► mvn compile ──► mvn test ──► mvn package ──► docker build
```

---

## ✨ Features

- ✅ Add student (ID, name, department, marks)
- ✅ View all students in formatted table
- ✅ Search by exact student ID
- ✅ Search by partial name (case-insensitive)
- ✅ Update name, department, and/or marks
- ✅ Delete student with confirmation prompt
- ✅ Automatic grade calculation (A+ / A / B / C / D / F)
- ✅ Input validation (empty fields, duplicate ID, marks 0–100)
- ✅ Descriptive error messages

---

## 📁 Project Structure

```
SmartStudentManagement/
├── pom.xml                                         Maven build descriptor
├── Dockerfile                                      Container image definition
├── Jenkinsfile                                     CI/CD pipeline definition
├── .gitignore                                      Git exclusions
├── README.md                                       This file
└── src/
    ├── main/java/com/vaishnavi/
    │   ├── Student.java                            Model + validation + grade
    │   ├── StudentService.java                     CRUD + search logic
    │   └── Main.java                               Console menu entry point
    └── test/java/com/vaishnavi/
        └── StudentServiceTest.java                 JUnit 5 test suite (17 tests)
```

---

## ⚙️ Setup and Execution

### Prerequisites

| Tool | Minimum Version | Check Command |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Apache Maven | 3.9.x | `mvn -version` |
| Docker Desktop | 20+ | `docker --version` |
| Git | 2.x | `git --version` |

### Build Commands (in order)

```bash
# 1. Clean previous build artifacts
mvn clean

# 2. Compile Java source files
mvn compile

# 3. Run all JUnit 5 tests
mvn test

# 4. Package into executable JAR
mvn package

# Output artifact:
#   target/smart-student-management-1.0.0.jar
```

### Run the Application

```bash
java -jar target/smart-student-management-1.0.0.jar
```

---

## 🧪 Testing

17 JUnit 5 tests covering:

| Test Category | Tests |
|---|---|
| Add & Retrieve | add student, find by ID, view all |
| Name Search | partial match, no match |
| Invalid Marks | below 0, above 100, boundary values |
| Duplicate IDs | reject second insertion |
| Update | full update, partial update |
| Delete | count reduction, post-delete lookup |
| Missing Student | find, update, delete non-existent |
| Grade Calculation | all six grade bands |
| Validation | empty ID, empty name |

Run tests:

```bash
mvn test
# Report: target/surefire-reports/
```

---

## 🐳 Docker Instructions

> **Concept:** An _image_ is a read-only template. A _container_ is a running instance of that image.

```bash
# Step 1: Build the JAR (required before Docker build)
mvn clean package

# Step 2: Build the Docker image
docker build -t smart-student-management:1.0.0 .

# Step 3: Run the interactive container
docker run -it --rm smart-student-management:1.0.0

# List images
docker images | findstr smart-student
```

> The `-it` flag is **required** for interactive console input inside Docker.
> `--rm` automatically removes the container when you exit.

---

## 🔧 Jenkins Pipeline

### Stages

| # | Stage | Description |
|---|---|---|
| 1 | Checkout | Clone source from GitHub |
| 2 | Compile | `mvn clean compile` |
| 3 | Test | `mvn test` + publish JUnit XML reports |
| 4 | Package | `mvn package` → JAR |
| 5 | Docker Build | `docker build` → image |
| 6 | Docker Verify | Confirm image exists |

### Jenkins Setup (Windows)

1. Download Jenkins LTS from https://www.jenkins.io/download/
2. Install as Windows service (or run: `java -jar jenkins.war`)
3. Access at http://localhost:8080
4. Install plugins: **Pipeline**, **Git**, **JUnit**, **Docker Pipeline**
5. Configure **Global Tool Configuration**:
   - JDK: name = `JDK17`, JAVA_HOME path
   - Maven: name = `Maven396`, MAVEN_HOME path
6. Add GitHub credentials (username + personal access token) with ID `github-credentials`
7. Create Pipeline job → SCM: Git → your repo URL → Jenkinsfile path

### Troubleshooting Common Errors

| Error | Cause | Fix |
|---|---|---|
| `mvn: not found` | Maven not in Jenkins PATH | Set Maven home in Global Tool Config |
| `JAVA_HOME not set` | JDK not configured | Add JDK in Global Tool Config |
| `docker: command not found` | Docker not accessible to Jenkins | Add Jenkins user to `docker-users` group or run Jenkins as admin |
| GitHub auth failure | Wrong credentials | Use Personal Access Token, not password |
| `Permission denied` (Docker) | Jenkins service account restricted | Restart Jenkins after adding to docker group |

---

## 🔖 Release Management

```bash
# Create and push a version tag
git tag -a v1.0.0 -m "Release v1.0.0 – initial working version"
git push origin v1.0.0

# On GitHub:
# Releases → Draft new release → Tag: v1.0.0
# Upload: target/smart-student-management-1.0.0.jar as release asset
```

> **Note:** A Git tag marks a commit. A GitHub Release is a separate UI feature
> that attaches downloadable files to that tag. A Docker image is a separate
> container artifact and is not the same as a GitHub Release.

---

## ⚠️ Known Limitations

- Data is **not persisted** — all records reset on restart.
- No database integration (by design, for simplicity).
- No web UI — console only.
- Docker image requires the JAR to be built locally before `docker build`.

## 🚀 Future Enhancements

- Persistent storage (SQLite / PostgreSQL)
- REST API with Spring Boot
- Web frontend (React or Thymeleaf)
- Docker Compose for multi-container setup
- GitHub Actions as an alternative CI/CD platform
- Role-based access (admin vs. viewer)

---

## 👩‍💻 Author

**Vaishnavi** — College Project, 2024  
Package: `com.vaishnavi`  
Artifact: `smart-student-management-1.0.0.jar`
