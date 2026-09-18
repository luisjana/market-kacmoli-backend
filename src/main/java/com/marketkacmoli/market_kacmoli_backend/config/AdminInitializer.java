package com.marketkacmoli.market_kacmoli_backend.config;

import com.marketkacmoli.market_kacmoli_backend.model.Admin;
import com.marketkacmoli.market_kacmoli_backend.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            String email = "kacmolimarket@gmail.com";
            String password = "admin123";

            if (adminRepository.findByEmail(email).isEmpty()) {

                Admin admin = new Admin();
                admin.setEmail(email);
                admin.setPassword(passwordEncoder.encode(password));

                adminRepository.save(admin);

                System.out.println("Admin u krijua me sukses.");
            }
        };
    }
}