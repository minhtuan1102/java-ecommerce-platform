package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed roles
        Role userRole = seedRole("ROLE_USER");
        Role adminRole = seedRole("ROLE_ADMIN");
        Role sellerRole = seedRole("ROLE_SELLER");

        // Seed admin user if not exists
        String adminEmail = "admin@ecommerce.com";
        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = User.builder()
                    .username("admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(userRole, adminRole))
                    .build();
            userRepository.save(admin);
            System.out.println("Seed: Created default admin user");
        }

        // Seed seller user if not exists
        String sellerEmail = "seller@ecommerce.com";
        if (!userRepository.existsByEmailIgnoreCase(sellerEmail)) {
            User seller = User.builder()
                    .username("seller")
                    .email(sellerEmail)
                    .password(passwordEncoder.encode("seller123"))
                    .roles(Set.of(userRole, sellerRole))
                    .build();
            userRepository.save(seller);
            System.out.println("Seed: Created default seller user");
        }
    }

    private Role seedRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder().name(roleName).build();
                    System.out.println("Seed: Created role " + roleName);
                    return roleRepository.save(role);
                });
    }
}
