## MySQL

### Table: patients
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- email: VARCHAR(255), Not Null, Unique
- password_hash: VARCHAR(255), Not Null

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: doctors
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- email: VARCHAR(255), Not Null, Unique
- password_hash: VARCHAR(255), Not Null
- specialization: VARCHAR(150), Not Null
- phone_number: VARCHAR(20), Null

### Table: admins
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- email: VARCHAR(255), Not Null, Unique
- password_hash: VARCHAR(255), Not Null

### Table: doctor_unavailability
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- start_time: DATETIME, Not Null
- end_time: DATETIME, Not Null
- reason: VARCHAR(255), Null

## MongoDB

### Collection: prescriptions
```json
{
  "_id": "ObjectId('64abc123456')",
  "appointmentId": 51,
  "patientId": 12,
  "doctorId": 7,
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "instructions": "Take 1 tablet every 6 hours.",
      "refillCount": 2
    }
  ],
  "doctorNotes": "Take with water after meals.",
  "issuedAt": "2026-03-19T10:30:00Z"
}
