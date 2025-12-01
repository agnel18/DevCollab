package tech.ignitr.habitus.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.ignitr.habitus.configuration.DatabaseException;
import tech.ignitr.habitus.data.user.User;
import tech.ignitr.habitus.data.user.UserRepository;
import tech.ignitr.habitus.web.user.LoginRequest;
import tech.ignitr.habitus.web.user.RegisterRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<AuthenticationResponse> authenticateUser(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));
            User user = repository.findByEmail(request.getEmail()).orElseThrow(()-> new DatabaseException("User not found", HttpStatus.NOT_FOUND));

            String jwtToken = tokenService.generateToken(user);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(jwtToken)
                    .user(user)
                    .build());
        } catch (AuthenticationException ignored) {
            return ResponseEntity.badRequest().build();
        } catch (DatabaseException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<AuthenticationResponse> registerUser(RegisterRequest request) {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .name(request.getName())
                .password(encoder.encode(request.getPassword()))
                .build();
        repository.saveAndFlush(newUser);
        String jwtToken = tokenService.generateToken(newUser);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwtToken)
                .user(newUser)
                .build());
    }
}
