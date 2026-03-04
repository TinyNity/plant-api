package org.ili.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a persisted refresh token linked to a {@link User}.
 * <p>
 * Storing refresh tokens in the database allows the server to revoke them
 * individually or in bulk (e.g., on logout), converting the otherwise stateless
 * JWT flow into a controlled, revocable one.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * The user this token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    /**
     * The raw JWT refresh-token string (stored for lookup and validation).
     */
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    public String token;

    /**
     * When the token was issued.
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    /**
     * When the token expires (mirrors the JWT exp claim).
     */
    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;
}
