// ─────────────────────────────────────────────────────────────────────────────
// Jenkinsfile – Smart Student Management System
//
// Pipeline stages:
//   1. Checkout    – clone from GitHub
//   2. Compile     – mvn compile
//   3. Test        – mvn test (JUnit 5)
//   4. Package     – mvn package
//   5. Docker Build – docker build (only if package succeeded)
//   6. Docker Verify – confirm image exists locally
//   7. Results     – publish JUnit XML reports
//
// Windows-compatible: uses `bat` steps.
// To run on Linux agents, replace every `bat '...'` with `sh '...'`
//
// Prerequisites in Jenkins:
//   • Global Tool Configuration:
//       - JDK:   name = "JDK17"    (point to Java 17 home)
//       - Maven: name = "Maven396" (point to Maven 3.9.6 home)
//   • Credentials:
//       - GitHub credentials (username/token) with ID "github-credentials"
//   • Plugins: Pipeline, Git, JUnit, Docker Pipeline
// ─────────────────────────────────────────────────────────────────────────────

pipeline {

    agent any

    // ── Tool references (must match Global Tool Configuration names) ──────────
    tools {
        jdk   'JDK17'
        maven 'Maven396'
    }

    environment {
        IMAGE_NAME    = 'smart-student-management'
        IMAGE_TAG     = '1.0.0'
        JAR_PATH      = 'target/smart-student-management-1.0.0.jar'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo '🔄 Checking out source code from GitHub...'
                checkout scm
                echo '✅ Checkout complete.'
            }
        }

        // ── Stage 2: Compile ──────────────────────────────────────────────────
        stage('Compile') {
            steps {
                echo '🔨 Compiling Java source files...'
                bat 'mvn clean compile -B'
                echo '✅ Compilation successful.'
            }
        }

        // ── Stage 3: Test ─────────────────────────────────────────────────────
        stage('Test') {
            steps {
                echo '🧪 Running JUnit 5 tests...'
                bat 'mvn test -B'
                echo '✅ All tests passed.'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    echo 'ℹ  JUnit test results published.'
                }
            }
        }

        // ── Stage 4: Package ──────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo '📦 Packaging application into JAR...'
                bat 'mvn package -DskipTests -B'
                echo "✅ JAR created: ${env.JAR_PATH}"
                bat "if exist ${env.JAR_PATH} (echo JAR found.) else (echo ERROR: JAR missing! && exit /b 1)"
            }
        }

        // ── Stage 5: Docker Build ─────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                bat "docker build -t ${env.IMAGE_NAME}:${env.IMAGE_TAG} ."
                echo "✅ Docker image built: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
            }
        }

        // ── Stage 6: Docker Verify ────────────────────────────────────────────
        stage('Docker Verify') {
            steps {
                echo '🔍 Verifying Docker image exists...'
                bat "docker image inspect ${env.IMAGE_NAME}:${env.IMAGE_TAG} > nul 2>&1 && echo Image verified. || (echo ERROR: Image not found! && exit /b 1)"
            }
        }
    }

    // ── Post-pipeline notifications ───────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════╗
║  ✅ PIPELINE SUCCEEDED                   ║
║  Build   : #${env.BUILD_NUMBER}          ║
║  Job     : ${env.JOB_NAME}              ║
║  Image   : ${env.IMAGE_NAME}:${env.IMAGE_TAG}  ║
╚══════════════════════════════════════════╝
"""
        }
        failure {
            echo """
╔══════════════════════════════════════════╗
║  ❌ PIPELINE FAILED                      ║
║  Build  : #${env.BUILD_NUMBER}           ║
║  Check console output for details.       ║
╚══════════════════════════════════════════╝
"""
        }
        always {
            echo 'Pipeline execution completed.'
        }
    }
}
