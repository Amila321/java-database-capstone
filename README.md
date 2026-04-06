# 🏥 Smart Clinic Management System

This project is the **final capstone project** of the *IBM Java
Developer Professional Certificate*. It represents a complete,
end-to-end full-stack application designed to simulate a real-world
healthcare system used by clinics to manage patients, doctors, and
appointments.

The application was built from scratch following modern software
engineering practices, including **layered architecture**, **RESTful API
design**, **database modeling**, **containerization**, and **CI
workflows**.

------------------------------------------------------------------------

## 📌 Project Overview

The **Smart Clinic Management System** is a web-based platform that
enables efficient management of outpatient clinic operations. It
supports multiple user roles such as:

-   **Admin**
-   **Doctor**
-   **Patient**

Each role has its own permissions and functionalities, allowing users
to:

-   Manage appointments\
-   Handle patient records\
-   Access dashboards\
-   Work with prescriptions and schedules

------------------------------------------------------------------------

## 🏗️ Architecture

The application follows a **three-tier architecture**:

-   **Presentation Layer** -- Thymeleaf views + REST clients\
-   **Application Layer** -- Spring Boot (controllers, services,
    business logic)\
-   **Data Layer** -- MySQL + MongoDB

➡️ Detailed architecture description is available in:\
👉 `shema-architecture.md`

------------------------------------------------------------------------

## 🗄️ Database Design

The system uses a **hybrid database approach**:

-   **MySQL** → relational data (patients, doctors, appointments,
    admins)\
-   **MongoDB** → document-based data (prescriptions)

➡️ Full database schema is available in:\
👉 `schema-design.md`

------------------------------------------------------------------------

## ⚙️ Application Features

All implemented and planned features are tracked using GitHub issues.

➡️ You can explore them here:\
👉 **Issues tab in this repository**

------------------------------------------------------------------------

## 📊 Sample Data

The project includes preloaded sample data such as:

-   Doctors\
-   Patients\
-   Appointments\
-   Admin account\
-   Login credentials

➡️ Located in:\
app/src/main/resources/data.sql

------------------------------------------------------------------------

## 🚀 Running the Application

You can run the entire application using Docker.

### ✅ Requirements

-   Docker installed and running

### ▶️ Run the project

    git clone https://github.com/Amila321/java-database-capstone.git
    cd app
    docker compose up --build

### ⛔ Stop the project

    docker compose down -v

### 🌐 Access the application

http://localhost:8080

------------------------------------------------------------------------

## 🔄 Continuous Integration (CI)

The project includes a **basic CI setup** using GitHub Actions located
in:

.github/workflows

The CI pipeline includes:

-   Backend compilation (Maven, Java 17)\
-   Java linting with Checkstyle\
-   Dockerfile linting (Hadolint)\
-   Frontend linting (HTML, CSS, JS)

------------------------------------------------------------------------

## 🎓 About the Capstone

This project was developed as part of the IBM Java Developer Capstone,
which involved:

-   Requirements analysis\
-   System architecture design\
-   Database modeling\
-   Backend development (Spring Boot)\
-   Frontend integration\
-   Docker deployment\
-   CI/CD setup

------------------------------------------------------------------------

## 💡 Summary

This project demonstrates:

-   Full-stack development with Java (Spring Boot)\
-   Clean architecture and separation of concerns\
-   Hybrid database design (SQL + NoSQL)\
-   REST API design and MVC integration\
-   Containerization with Docker\
-   CI workflows with GitHub Actions
