package org.ili.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
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

/**
 * Service class that handles the core business logic for authentication.
 * <p>
 * This includes user registration, password hashing/verification, and managing
 * the issuance of JWT access and refresh tokens.
 */
@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://xmobilite.com/issuer")
    String issuer;

    @Inject
    io.smallrye.jwt.auth.principal.JWTParser jwtParser;

    /**
     * Registers a new user.
     * <p>
     * Checks if the given email or username already exists. If not, it creates
     * a new {@link User} entity, hashes the password using bcrypt, persists the
     * user to the database, and then generates access and refresh tokens for
     * immediate login.
     *
     * @param request the registration details.
     * @return {@link AuthResponse} containing the access and refresh tokens.
     * @throws WebApplicationException with HTTP 409 Conflict if email or
     * username existing.
     */
    @Transactional
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

    /**
     * Authenticates an existing user.
     * <p>
     * Attempts to find a user by their email address and then verifies the
     * provided plaintext password against the stored bcrypt hash. Upon success,
     * newly generated access and refresh tokens are returned.
     *
     * @param request the login credentials.
     * @return {@link AuthResponse} containing the access and refresh tokens.
     * @throws WebApplicationException with HTTP 401 Unauthorized for invalid
     * credentials.
     */
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

    /**
     * Refreshes JWT tokens using a valid refresh token.
     * <p>
     * Parses the provided token securely using the JWT parser. Extracts the
     * subject (User ID), verifies the user exists in the database, and issues a
     * fresh pair of tokens.
     *
     * @param refreshToken the unexpired refresh JWT token string.
     * @return {@link AuthResponse} containing the new access and refresh
     * tokens.
     * @throws WebApplicationException with HTTP 401 Unauthorized if token is
     * invalid, expired, or user not found.
     */
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

    /**
     * Logs out the user with the given ID.
     * <p>
     * This is an intentional stub for potential future stateful token
     * management, allowing refresh token invalidation in a data store or cache.
     *
     * @param userId the ID of the user requesting logout.
     */
    public void logout(UUID userId) {
        // logic to revoke tokens if stateful
    }

    /**
     * Gets public profile information for a user.
     *
     * @param userId the ID of the user.
     * @return {@link UserResponse} dto containing public user information.
     * @throws WebApplicationException with HTTP 404 Not Found if the user
     * doesn't exist.
     */
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));

        return UserResponse.builder()
                .id(user.id)
                .username(user.username)
                .email(user.email)
                .build();
    }

    /**
     * Generates a signed JWT with standard claims.
     *
     * @param userId the user IDs to set as the subject.
     * @param email the user email to set as the UPN.
     * @param duration the token's lifetime duration.
     * @return a signed JWT string.
     */
    private String generateToken(UUID userId, String email, Duration duration) {
        return Jwt.issuer(issuer)
                .subject(userId.toString())
                .upn(email)
                .groups(Set.of("MEMBER"))
                .expiresIn(duration.toSeconds())
                .sign();
    }
}
