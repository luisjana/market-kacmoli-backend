package com.marketkacmoli.market_kacmoli_backend.controller;

import com.marketkacmoli.market_kacmoli_backend.model.Admin;
import com.marketkacmoli.market_kacmoli_backend.repository.AdminRepository;
import com.marketkacmoli.market_kacmoli_backend.service.JwtService;
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

        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<Admin> adminOptional = adminRepository.findByEmail(email);

        if (adminOptional.isEmpty()) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Email ose password gabim"));
        }

        Admin admin = adminOptional.get();

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Email ose password gabim"));
        }

        String token = jwtService.generateToken(admin.getEmail());

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Login successful");
        response.put("adminId", admin.getId());
        response.put("email", admin.getEmail());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}