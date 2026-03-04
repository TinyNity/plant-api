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
import org.ili.entity.RefreshToken;
import org.ili.entity.User;
import org.ili.repository.RefreshTokenRepository;
import org.ili.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Service class that handles the core business logic for authentication.
 * <p>
 * This includes user registration, password hashing/verification, and managing
 * the issuance of JWT access and refresh tokens.
 * <p>
 * Refresh tokens are persisted in the database so they can be revoked on
 * logout, making the otherwise stateless JWT flow controllable server-side.
 */
@ApplicationScoped
public class AuthService {

    /**
     * Lifetime of a short-lived access token.
     */
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);

    /**
     * Lifetime of a long-lived refresh token.
     */
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    @Inject
    UserRepository userRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://xmobilite.com/issuer")
    String issuer;

    @Inject
    io.smallrye.jwt.auth.principal.JWTParser jwtParser;

    @Inject
    JsonWebToken jwt;

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

        return buildAndPersistTokenPair(user);
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
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED));

        if (!BcryptUtil.matches(request.getPassword(), user.password)) {
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        }

        return buildAndPersistTokenPair(user);
    }

    /**
     * Refreshes JWT tokens using a valid refresh token.
     * <p>
     * Parses the provided token securely using the JWT parser. Extracts the
     * subject (User ID), verifies the token exists in the database (i.e., has
     * not been revoked), and issues a fresh pair of tokens. The old token is
     * deleted as part of token rotation.
     *
     * @param rawRefreshToken the unexpired refresh JWT token string.
     * @return {@link AuthResponse} containing the new access and refresh
     * tokens.
     * @throws WebApplicationException with HTTP 401 Unauthorized if token is
     * invalid, expired, revoked, or user not found.
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        try {
            JsonWebToken jwt = jwtParser.parse(rawRefreshToken);
            UUID userId = UUID.fromString(jwt.getSubject());

            // Reject the token if it was revoked (not present in the DB)
            RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                    .orElseThrow(() -> new WebApplicationException("Refresh token has been revoked", Response.Status.UNAUTHORIZED));

            User user = userRepository.findByIdOptional(userId)
                    .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.UNAUTHORIZED));

            // Rotate: delete the old token before issuing a new pair
            refreshTokenRepository.delete(stored);

            return buildAndPersistTokenPair(user);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Invalid refresh token", Response.Status.UNAUTHORIZED);
        }
    }

    /**
     * Logs out the user with the given ID.
     * <p>
     * Revokes all active refresh tokens for the user by deleting them from the
     * database. This prevents any further token-refresh operations, effectively
     * terminating all active sessions for that user.
     *
     * @param userId the ID of the user requesting logout.
     */
    @Transactional
    public void logout(UUID userId) {
        long deleted = refreshTokenRepository.deleteAllByUserId(userId);
        if (deleted == 0) {
            // The user may have already logged out or never had a refresh token persisted.
            // This is not an error — we treat logout as idempotent.
        }
    }

    /**
     * Gets public profile information for a user.
     *
     * @param userId the ID of the user.
     * @return {@link UserResponse} dto containing public user information.
     * @throws WebApplicationException with HTTP 404 Not Found if the user
     * doesn't exist.
    */
   public User getCurrentUser() {
       return userRepository.findById(getCurrentUserId());
   }
   
    public UUID getCurrentUserId() {
        return UUID.fromString(jwt.getSubject());
    }


    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------
    /**
     * Generates a signed access token and a signed refresh token, persists the
     * refresh token to the database, and returns both wrapped in an
     * {@link AuthResponse}.
     *
     * @param user the authenticated user.
     * @return {@link AuthResponse} with the new token pair.
     */
    private AuthResponse buildAndPersistTokenPair(User user) {
        Instant now = Instant.now();

        String accessToken = generateToken(user.id, user.email, ACCESS_TOKEN_DURATION);
        String refreshToken = generateToken(user.id, user.email, REFRESH_TOKEN_DURATION);

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .createdAt(now)
                .expiresAt(now.plus(REFRESH_TOKEN_DURATION))
                .build();

        refreshTokenRepository.persist(tokenEntity);

        return new AuthResponse(accessToken, refreshToken);
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
