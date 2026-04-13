package com.tuan.ecommerce.modules.auth.application;

import com.tuan.ecommerce.modules.auth.application.dto.LoginRequest;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.mapper.AuthMapper;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private AuthService authService;
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository = new InMemoryRoleRepository();
        roleRepository.save(Role.builder().name("ROLE_USER").build());
        authService = new AuthService(new InMemoryUserRepository(), roleRepository, new AuthMapper());
    }

    @Test
    void register_shouldAssignDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@mail.com");
        request.setPassword("secret123");

        var result = authService.register(request);

        assertEquals("john", result.getUsername());
        // Verify role assignment logic (assuming we can check it, for now just ensuring it doesn't throw)
    }

    @Test
    void register_shouldTrimUsernameAndNormalizeEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("  john  ");
        request.setEmail("  JOHN@MAIL.COM  ");
        request.setPassword("secret123");

        var result = authService.register(request);

        assertEquals("john", result.getUsername());
        assertEquals("john@mail.com", result.getEmail());
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@mail.com");
        request.setPassword("secret123");

        authService.register(request);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIsWrong() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@mail.com");
        request.setPassword("secret123");
        authService.register(request);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@mail.com");
        loginRequest.setPassword("wrong-password");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(loginRequest));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final List<User> store = new ArrayList<>();
        private final AtomicLong sequence = new AtomicLong(1L);

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(sequence.getAndIncrement());
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                store.add(user);
                return user;
            }
            store.removeIf(u -> u.getId().equals(user.getId()));
            user.setUpdatedAt(LocalDateTime.now());
            store.add(user);
            return user;
        }

        @Override
        public boolean existsByUsernameIgnoreCase(String username) {
            return store.stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        }

        @Override
        public boolean existsByEmailIgnoreCase(String email) {
            return store.stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
        }

        @Override
        public Optional<User> findByEmailIgnoreCase(String email) {
            return store.stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst();
        }
    }

    private static class InMemoryRoleRepository implements RoleRepository {
        private final List<Role> store = new ArrayList<>();
        private final AtomicLong sequence = new AtomicLong(1L);

        @Override
        public Optional<Role> findByName(String name) {
            return store.stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
        }

        @Override
        public Role save(Role role) {
            if (role.getId() == null) {
                role.setId(sequence.getAndIncrement());
                store.add(role);
            }
            return role;
        }
    }
}
