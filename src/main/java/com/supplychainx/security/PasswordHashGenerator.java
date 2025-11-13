package com.supplychainx.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin123";
        String adminHash = passwordEncoder.encode(adminPassword);
        System.out.println("Mot de passe : " + adminPassword);
        System.out.println("Hash BCrypt  : " + adminHash);
        String[] passwords = {"password123", "test1234", "securepass"};
        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("Mot de passe : " + password);
            System.out.println("Hash         : " + hash);
            System.out.println();
        }
        for (int i = 1; i <= 3; i++) {
            String hash = passwordEncoder.encode("admin123");
            System.out.println("Hash " + i + "       : " + hash);
        }
    }
}
