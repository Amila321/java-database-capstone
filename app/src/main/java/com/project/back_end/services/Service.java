package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @Transactional(readOnly = true)
    public Map<String, String> validateToken(String token, String role) {
        Map<String, String> response = new HashMap<>();

        try {
            if (token == null || token.isBlank()) {
                response.put("message", "Token is missing.");
                return response;
            }

            boolean valid = tokenService.validateToken(token, role);

            if (!valid) {
                response.put("message", "Invalid or expired token.");
            }

            return response;
        } catch (Exception e) {
            response.put("message", "Error validating token.");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateAdmin(Admin admin) {
        Map<String, String> response = new HashMap<>();

        try {
            if (admin == null || admin.getUsername() == null || admin.getPassword() == null) {
                response.put("message", "Username and password are required.");
                return ResponseEntity.badRequest().body(response);
            }

            Admin existingAdmin = adminRepository.findByUsername(admin.getUsername());

            if (existingAdmin == null) {
                response.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!existingAdmin.getPassword().equals(admin.getPassword())) {
                response.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(existingAdmin.getUsername(), "admin");
            response.put("message", "Login successful.");
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Internal server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = isProvided(name);
        boolean hasSpecialty = isProvided(specialty);
        boolean hasTime = isProvided(time);

        if (!hasName && !hasSpecialty && !hasTime) {
            Map<String, Object> result = new HashMap<>();
            result.put("doctors", doctorService.getDoctors());
            return result;
        }

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        }

        if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        }

        if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        }

        if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        }

        if (hasName) {
            return doctorService.findDoctorByName(name);
        }

        if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        }

        return doctorService.filterDoctorsByTime(time);
    }

    @Transactional(readOnly = true)
    public Map<String, String> validateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        try {
            if (appointment == null) {
                response.put("message", "Appointment is required.");
                return response;
            }

            if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
                response.put("message", "Doctor ID is required.");
                return response;
            }

            if (appointment.getAppointmentTime() == null) {
                response.put("message", "Appointment time is required.");
                return response;
            }

            Long doctorId = appointment.getDoctor().getId();
            Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);

            if (doctorOptional.isEmpty()) {
                response.put("message", "Doctor not found.");
                return response;
            }

            LocalDate date = appointment.getAppointmentTime().toLocalDate();
            List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

            String requestedTime = appointment.getAppointmentTime()
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));

            boolean validSlot = availableSlots.stream()
                    .anyMatch(slot -> slot.equals(requestedTime));

            if (!validSlot) {
                response.put("message", "Selected appointment slot is not available.");
            }

            return response;

        } catch (Exception e) {
            response.put("message", "Error validating appointment.");
            return response;
        }
    }

    @Transactional(readOnly = true)
    public boolean validatePatient(Patient patient) {
        try {
            if (patient == null || patient.getEmail() == null || patient.getPhone() == null) {
                return false;
            }

            Patient existingPatient = patientRepository.findByEmailOrPhone(
                    patient.getEmail(),
                    patient.getPhone()
            );

            return existingPatient == null;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            if (login == null || login.getEmail() == null || login.getPassword() == null) {
                response.put("message", "Email and password are required.");
                return ResponseEntity.badRequest().body(response);
            }

            Patient patient = patientRepository.findByEmail(login.getEmail());

            if (patient == null) {
                response.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(patient.getEmail(), "patient");
            response.put("message", "Login successful.");
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Internal server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String doctorName, String token) {
        try {
            String email = tokenService.extractEmail(token);

            if (email == null || email.isBlank()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Patient not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            boolean hasCondition = isProvided(condition);
            boolean hasDoctorName = isProvided(doctorName);

            if (!hasCondition && !hasDoctorName) {
                return patientService.getPatientAppointment(patient.getId(), token);
            }

            if (hasCondition && !hasDoctorName) {
                return patientService.filterByCondition(condition, patient.getId());
            }

            if (!hasCondition && hasDoctorName) {
                return patientService.filterByDoctor(doctorName, patient.getId());
            }

            return patientService.filterByDoctorAndCondition(condition, doctorName, patient.getId());

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error filtering patient appointments.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isProvided(String value) {
        return value != null
                && !value.isBlank()
                && !"null".equalsIgnoreCase(value.trim());
    }
}