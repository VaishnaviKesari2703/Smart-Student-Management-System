// Declarative pipeline: Checkout -> Build -> Test -> Package -> Docker Build -> Release -> Deploy
// Jenkins prerequisites (Manage Jenkins > Tools): a JDK named "JDK17" and a Maven named "Maven3".
// Docker (with Compose v2) must be installed on the agent. Works on Windows (bat) and Linux (sh).
pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters {
        string(name: 'GIT_URL', defaultValue: '', description: 'Optional: GitHub repository URL. Leave empty to use the job SCM configuration.')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to build (used only with GIT_URL)')
        string(name: 'GIT_CREDENTIALS_ID', defaultValue: '', description: 'Optional: Jenkins credentials ID for a private repository (never hardcode secrets)')
    }

    environment {
        APP_NAME  = 'smart-student-management'
        VERSION   = '1.0.0'
        IMAGE     = "${APP_NAME}:${VERSION}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    if (params.GIT_URL?.trim()) {
                        def cfg = [url: params.GIT_URL.trim()]
                        if (params.GIT_CREDENTIALS_ID?.trim()) { cfg.credentialsId = params.GIT_CREDENTIALS_ID.trim() }
                        git branch: params.GIT_BRANCH, url: cfg.url, credentialsId: cfg.credentialsId
                    } else {
                        checkout scm
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling with Maven (clean compile)...'
                runCmd('mvn -B clean compile')
            }
        }

        stage('Test') {
            steps {
                echo 'Running JUnit 5 tests...'
                runCmd('mvn -B test')
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Creating executable Spring Boot JAR (tests already passed)...'
                runCmd('mvn -B package -DskipTests')
                archiveArtifacts artifacts: "target/${APP_NAME}-${VERSION}.jar", fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image ${IMAGE}..."
                runCmd("docker build -t ${IMAGE} .")
            }
        }

        stage('Release') {
            steps {
                echo "Tagging image as ${APP_NAME}:${VERSION} and latest..."
                runCmd("docker tag ${IMAGE} ${APP_NAME}:latest")
                runCmd("docker image ls ${APP_NAME}")
            }
        }

        stage('Deployment') {
            steps {
                echo 'Deploying with Docker Compose (data persists in the student-data volume)...'
                runCmd('docker compose up -d --force-recreate')
                runCmd('docker compose ps')
            }
        }
    }

    post {
        success { echo "SUCCESS: ${APP_NAME} ${VERSION} built, tested and deployed at http://localhost:8080" }
        failure { echo 'FAILURE: pipeline stopped. Check the stage logs above (compile or test failure).' }
        always  { echo "Pipeline finished with status: ${currentBuild.currentResult}" }
    }
}

// Runs a shell command on either Linux/macOS (sh) or Windows (bat) agents.
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}
