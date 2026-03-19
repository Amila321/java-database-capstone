## MySQL

### Table: patients
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(10), Not Null
- address: VARCHAR(255), Not Null

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: doctors
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- specialty: VARCHAR(50), Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(10), Not Null

### Table: doctor_available_times
- doctor_id: BIGINT, Foreign Key → doctors(id)
- available_time: VARCHAR(50), Not Null

### Table: admins
- id: BIGINT, Primary Key, Auto Increment
- username: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null


## MongoDB

### Collection: prescriptions
```json
{
  "_id": "64abc123456",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours."
}