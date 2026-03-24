package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String identifier) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String identifier, String role) {
        return generateToken(identifier);
    }

    public String extractIdentifier(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractIdentifier(token);

            if (identifier == null || identifier.isBlank()) {
                return false;
            }

            if ("admin".equalsIgnoreCase(user)) {
                Admin admin = adminRepository.findByUsername(identifier);
                return admin != null;
            }

            if ("doctor".equalsIgnoreCase(user)) {
                Doctor doctor = doctorRepository.findByEmail(identifier);
                return doctor != null;
            }

            if ("patient".equalsIgnoreCase(user)) {
                Patient patient = patientRepository.findByEmail(identifier);
                return patient != null;
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}