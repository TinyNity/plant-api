package org.ili.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.ili.dto.AuthResponse;
import org.ili.dto.LoginRequest;
import org.ili.dto.RegisterRequest;
import org.ili.dto.UserResponse;
import org.ili.entity.User;
import org.ili.repository.UserRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://xmobilite.com/issuer")
    String issuer;

    @Inject
    io.smallrye.jwt.auth.principal.JWTParser jwtParser;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.find("email", request.getEmail()).firstResultOptional().isPresent()) {
            throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
        }

        if (userRepository.find("username", request.getUsername()).firstResultOptional().isPresent()) {
            throw new WebApplicationException("Username already exists", Response.Status.CONFLICT);
        }

        User user = new User();
        user.email = request.getEmail();
        user.username = request.getUsername();
        user.password = BcryptUtil.bcryptHash(request.getPassword());

        userRepository.persist(user);

        String accessToken = generateToken(user.id, user.email, Duration.ofMinutes(15));
        String refreshToken = generateToken(user.id, user.email, Duration.ofDays(7));

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED));

        if (!BcryptUtil.matches(request.getPassword(), user.password)) {
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        }

        String accessToken = generateToken(user.id, user.email, Duration.ofMinutes(15));
        String refreshToken = generateToken(user.id, user.email, Duration.ofDays(7));

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            JsonWebToken jwt = jwtParser.parse(refreshToken);
            UUID userId = UUID.fromString(jwt.getSubject());
            User user = userRepository.findByIdOptional(userId)
                    .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.UNAUTHORIZED));

            String newAccessToken = generateToken(user.id, user.email, Duration.ofMinutes(15));
            String newRefreshToken = generateToken(user.id, user.email, Duration.ofDays(7));

            return new AuthResponse(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid refresh token", Response.Status.UNAUTHORIZED);
        }
    }

    public void logout(UUID userId) {
        // logic to revoke tokens if stateful
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));

        return UserResponse.builder()
                .id(user.id)
                .username(user.username)
                .email(user.email)
                .build();
    }

    private String generateToken(UUID userId, String email, Duration duration) {
        return Jwt.issuer(issuer)
                .subject(userId.toString())
                .upn(email)
                .groups(Set.of("MEMBER"))
                .expiresIn(duration.toSeconds())
                .sign();
    }
}
