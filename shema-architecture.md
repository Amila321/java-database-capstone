# Application Architecture Overview

---

## Architecture Summary

This **Smart Clinic Management System** is designed as a **three-tier Spring Boot application** with a clear separation between the **presentation**, **application**, and **data** layers.

At the presentation layer, the system combines **MVC** and **REST** approaches. **Thymeleaf templates** are used to render server-side views such as the **Admin Dashboard** and **Doctor Dashboard**, while **REST APIs** support modules like **appointments**, **patient records**, and other client-facing features that return **JSON** responses.

Incoming requests are handled by the appropriate **MVC** or **REST controllers**, which then delegate processing to a shared **service layer**. This layer contains the main **business logic**, **validation**, and **workflow coordination**.

The service layer communicates with **repositories** responsible for data persistence across two databases. **MySQL** stores structured relational data such as **admins**, **doctors**, **patients**, **roles**, and **appointments**, using **Spring Data JPA** with entity-based mapping. **MongoDB** is used for more flexible, document-oriented data such as **prescriptions**, which are represented as document models.

This architecture improves the system's **maintainability**, **testability**, and **scalability**, while also supporting **Docker-based deployment** and **CI/CD workflows** for automated building and testing.

---

## Numbered Flow of Data and Control

1. The user opens a page or sends a request, such as **AdminDashboard**, **DoctorDashboard**, or **Appointments**.
2. **Spring Boot** routes the request to the appropriate **MVC** or **REST controller**.
3. The controller forwards the request to the **service layer**.
4. The service layer handles the main **business logic** and **validation**.
5. It then calls the appropriate **repository** to retrieve or store data.
6. **MySQL** stores relational data such as doctors, patients, and appointments, while **MongoDB** stores prescription data.
7. The result is returned to the user either as an **HTML page** through **Thymeleaf** or as **JSON** through a **REST API**.
