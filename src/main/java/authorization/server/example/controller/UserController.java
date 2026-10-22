package authorization.server.example.controller;

import authorization.server.example.dto.RegisterRequest;
import authorization.server.example.entity.User;
import authorization.server.example.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be empty"));
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
        }

        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setUsername(request.getUsername().trim());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        String assignedRoles = (request.getRoles() != null && !request.getRoles().isBlank())
                ? request.getRoles().trim()
                : "USER";
        newUser.setRoles(assignedRoles);
        newUser.setEnabled(true);

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully",
                "userId", newUser.getId(),
                "username", newUser.getUsername(),
                "roles", newUser.getRoles()
        ));
    }
}
