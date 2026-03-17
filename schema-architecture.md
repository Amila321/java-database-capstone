Architecture summary
This Smart Clinic Management System is built as a three-tier Spring Boot application with a clear separation between the presentation, application, and data layers. On the presentation side, the system uses a mix of MVC and REST: Thymeleaf templates render server-side pages such as the Admin and Doctor dashboards, while REST APIs handle modules like appointments, patient records, and other client-facing features that return JSON responses. Incoming requests are processed by either MVC or REST controllers, which then pass the work to a shared service layer where the main business logic, validation, and workflow coordination are implemented.

The service layer communicates with repositories that manage data persistence in two different databases. MySQL is used for structured relational data such as admins, doctors, patients, roles, and appointments, with Spring Data JPA and entity classes handling the mapping. MongoDB is used for more flexible document-based data such as prescriptions, which are modeled as document objects. This architecture makes the application easier to maintain, test, and scale, while also fitting well with Docker-based deployment and CI/CD workflows for automated building and testing.

I can also make it sound more student-like, more formal, or simpler for submission.


Numbered flow of data and control

1. User opens a page or sends a request, such as AdminDashboard, DoctorDashboard, or Appointments.

2. Spring Boot routes the request to the correct MVC or REST controller.

3. The controller passes the request to the service layer.

4. The service layer handles the main business logic and validation.

5. Then it calls the proper repository to get or save data.

6. MySQL stores relational data like doctors, patients, and appointments, while MongoDB stores prescriptions.

7. The result is returned to the user either as an HTML page through Thymeleaf or as JSON through a REST API.