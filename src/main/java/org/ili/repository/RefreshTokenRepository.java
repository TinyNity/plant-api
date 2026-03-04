package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link RefreshToken} persistence operations.
 */
@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepositoryBase<RefreshToken, UUID> {

    /**
     * Finds a token record by its raw JWT string value.
     *
     * @param token the raw JWT string.
     * @return an {@link Optional} containing the matching token if found.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }

    /**
     * Deletes all refresh tokens belonging to a given user.
     * <p>
     * Called on logout to invalidate every active session for that user.
     *
     * @param userId the ID of the user whose tokens should be revoked.
     * @return the number of deleted records.
     */
    public long deleteAllByUserId(UUID userId) {
        return delete("user.id", userId);
    }
}
