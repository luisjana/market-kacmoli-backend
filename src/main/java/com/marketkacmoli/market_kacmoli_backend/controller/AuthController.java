package com.marketkacmoli.market_kacmoli_backend.controller;

import com.marketkacmoli.market_kacmoli_backend.model.Admin;
import com.marketkacmoli.market_kacmoli_backend.repository.AdminRepository;
import com.marketkacmoli.market_kacmoli_backend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            System.out.println(">>> Login attempt for email: " + email);

            Optional<Admin> adminOptional = adminRepository.findByEmail(email);

            if (adminOptional.isEmpty()) {
                System.out.println(">>> User not found in DB");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Email ose password gabim"));
            }

            Admin admin = adminOptional.get();

            if (!passwordEncoder.matches(password, admin.getPassword())) {
                System.out.println(">>> Password does not match");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Email ose password gabim"));
            }

            String token = jwtService.generateToken(admin.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("adminId", admin.getId());
            response.put("email", admin.getEmail());
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Printon gabimin e saktë në Render Logs në vend të 500 generik
            System.err.println(">>> ERROR DURING LOGIN: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal Error"));
        }
    }
}