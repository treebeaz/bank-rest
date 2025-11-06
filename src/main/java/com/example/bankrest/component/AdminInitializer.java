package com.example.bankrest.component;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostConstruct
    @Transactional
    public void createSuperAdmin() {
        if(!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .firstname("System")
                    .lastname("Administrator")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Super admin created: {}", admin.getUsername());
        }
        else {
            log.info("Super admin already exists");
        }
    }
}
