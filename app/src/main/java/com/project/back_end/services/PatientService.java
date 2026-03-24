package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null || email.isBlank()) {
                response.put("message", "Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("message", "Patient not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!patient.getId().equals(id)) {
                response.put("message", "Unauthorized access.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Appointment> appointments = appointmentRepository.findByPatient_Id(id);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::mapToDTO)
                    .toList();

            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error retrieving appointments.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status;

            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return ResponseEntity.badRequest().body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::mapToDTO)
                    .toList();

            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error filtering appointments by condition.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::mapToDTO)
                    .toList();

            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error filtering appointments by doctor.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status;

            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return ResponseEntity.badRequest().body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::mapToDTO)
                    .toList();

            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error filtering appointments by doctor and condition.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = tokenService.extractEmail(token);
            if (email == null || email.isBlank()) {
                response.put("message", "Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("message", "Patient not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("patient", patient);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error retrieving patient details.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor() != null ? appointment.getDoctor().getId() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getPatient() != null ? appointment.getPatient().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getEmail() : null,
                appointment.getPatient() != null ? appointment.getPatient().getPhone() : null,
                appointment.getPatient() != null ? appointment.getPatient().getAddress() : null,
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}