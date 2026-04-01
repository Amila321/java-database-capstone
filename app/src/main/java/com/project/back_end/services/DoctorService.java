package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
        if (doctorOptional.isEmpty()) {
            return new ArrayList<>();
        }

        Doctor doctor = doctorOptional.get();
        List<String> availableSlots = new ArrayList<>();

        if (doctor.getAvailableTimes() != null) {
            // Extract only the start time from slots (handle both "HH:mm" and "HH:mm-HH:mm" formats)
            for (String slot : doctor.getAvailableTimes()) {
                if (slot != null && !slot.isBlank()) {
                    String startTime = slot.contains("-") ? slot.split("-")[0].trim() : slot.trim();
                    availableSlots.add(startTime);
                }
            }
        }

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Set<String> bookedSlots = new HashSet<>();

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentTime() != null) {
                bookedSlots.add(appointment.getAppointmentTime().toLocalTime().format(formatter));
            }
        }

        availableSlots.removeIf(bookedSlots::contains);
        return availableSlots;
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getEmail() == null) {
                return 0;
            }

            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if (existingDoctor != null) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getId() == null) {
                return -1;
            }

            Optional<Doctor> existingDoctor = doctorRepository.findById(doctor.getId());
            if (existingDoctor.isEmpty()) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        doctors.forEach(doctor -> {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        });

        return doctors;
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> doctorOptional = doctorRepository.findById(id);
            if (doctorOptional.isEmpty()) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctor_Id(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        if (login == null || login.getEmail() == null || login.getPassword() == null) {
            response.put("message", "Email and password are required.");
            return ResponseEntity.badRequest().body(response);
        }

        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor == null) {
            response.put("message", "Doctor not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!doctor.getPassword().equals(login.getPassword())) {
            response.put("message", "Invalid credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = tokenService.generateToken(doctor.getEmail(), "doctor");
        response.put("message", "Login successful.");
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name == null ? "" : name);
        response.put("doctors", doctors);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                name == null ? "" : name,
                specialty
        );

        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findByNameLike(name == null ? "" : name);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);

        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                name == null ? "" : name,
                specilty
        );

        response.put("doctors", doctors);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);

        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        response.put("doctors", doctors);

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findAll();
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);

        response.put("doctors", filtered);
        return response;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (doctors == null || amOrPm == null || amOrPm.isBlank()) {
            return doctors != null ? doctors : new ArrayList<>();
        }

        String normalized = amOrPm.trim().toUpperCase();
        List<Doctor> filteredDoctors = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<String> availableTimes = doctor.getAvailableTimes();
            if (availableTimes == null || availableTimes.isEmpty()) {
                continue;
            }

            boolean matches = availableTimes.stream().anyMatch(time -> {
                try {
                    int hour = Integer.parseInt(time.split(":")[0]);

                    if ("AM".equals(normalized)) {
                        return hour < 12;
                    } else if ("PM".equals(normalized)) {
                        return hour >= 12;
                    }

                    return false;
                } catch (Exception e) {
                    return false;
                }
            });

            if (matches) {
                filteredDoctors.add(doctor);
            }
        }

        return filteredDoctors;
    }
}