package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final com.project.back_end.services.Service sharedService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              com.project.back_end.services.Service sharedService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.sharedService = sharedService;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            Map<String, String> errors = sharedService.validateAppointment(appointment);
            if (!errors.isEmpty()) {
                return 0;
            }

            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        try {
            if (appointment == null || appointment.getId() == null) {
                response.put("message", "Appointment ID is required.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Appointment> existingOptional = appointmentRepository.findById(appointment.getId());
            if (existingOptional.isEmpty()) {
                response.put("message", "Appointment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Appointment existingAppointment = existingOptional.get();

            Map<String, String> validationErrors = sharedService.validateAppointment(appointment);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(validationErrors);
            }

            if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
                response.put("message", "Doctor ID is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
                response.put("message", "Patient ID is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (doctorRepository.findById(appointment.getDoctor().getId()).isEmpty()) {
                response.put("message", "Doctor not found.");
                return ResponseEntity.badRequest().body(response);
            }

            if (patientRepository.findById(appointment.getPatient().getId()).isEmpty()) {
                response.put("message", "Patient not found.");
                return ResponseEntity.badRequest().body(response);
            }

            if (existingAppointment.getPatient() != null
                    && existingAppointment.getPatient().getId() != null
                    && !existingAppointment.getPatient().getId().equals(appointment.getPatient().getId())) {
                response.put("message", "Patient ID does not match the existing appointment.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            LocalDateTime appointmentTime = appointment.getAppointmentTime();
            if (appointmentTime == null) {
                response.put("message", "Appointment time is required.");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDateTime start = appointmentTime.toLocalDate().atStartOfDay();
            LocalDateTime end = appointmentTime.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);

            List<Appointment> doctorAppointments =
                    appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                            appointment.getDoctor().getId(),
                            start,
                            end
                    );

            boolean slotAlreadyBooked = doctorAppointments.stream()
                    .anyMatch(a ->
                            !a.getId().equals(appointment.getId())
                                    && a.getAppointmentTime().equals(appointment.getAppointmentTime())
                    );

            if (slotAlreadyBooked) {
                response.put("message", "This appointment slot is already booked.");
                return ResponseEntity.badRequest().body(response);
            }

            appointmentRepository.save(appointment);
            response.put("message", "Appointment updated successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error updating appointment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        try {
            if (!tokenService.validateToken(token, "patient")) {
                response.put("message", "Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
            if (appointmentOptional.isEmpty()) {
                response.put("message", "Appointment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String patientEmail = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(patientEmail);

            if (patient == null) {
                response.put("message", "Patient not found.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Appointment appointment = appointmentOptional.get();

            if (appointment.getPatient() == null
                    || appointment.getPatient().getId() == null
                    || !appointment.getPatient().getId().equals(patient.getId())) {
                response.put("message", "You are not allowed to cancel this appointment.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error cancelling appointment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!tokenService.validateToken(token, "doctor")) {
                response.put("appointments", List.of());
                response.put("message", "Invalid token.");
                return response;
            }

            String doctorEmail = tokenService.extractIdentifier(token);
            Doctor doctor = doctorRepository.findByEmail(doctorEmail);

            if (doctor == null) {
                response.put("appointments", List.of());
                response.put("message", "Doctor not found.");
                return response;
            }

            Long doctorId = doctor.getId();

            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end = targetDate.plusDays(1).atStartOfDay().minusNanos(1);

            List<Appointment> appointments;

            if (pname == null || pname.isBlank() || "null".equalsIgnoreCase(pname)) {
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctorId, start, end
                );
            } else {
                appointments = appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                doctorId, pname, start, end
                        );
            }

            response.put("appointments", appointments);
            return response;

        } catch (Exception e) {
            response.put("appointments", List.of());
            response.put("message", "Error retrieving appointments.");
            return response;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(int status, long id) {
        Map<String, String> response = new HashMap<>();

        try {
            Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
            if (appointmentOptional.isEmpty()) {
                response.put("message", "Appointment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            appointmentRepository.updateStatus(status, id);
            response.put("message", "Appointment status updated successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error updating appointment status.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}