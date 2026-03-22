package org.ili.repository;

import org.ili.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;
/**
 * Repository for {@link User} persistence and lookup operations.
 */

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    /**
     * Finds a user by email.
     *
     * @param email email to search.
     * @return matching user when present.
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Finds a user by username.
     *
     * @param username username to search.
     * @return matching user when present.
     */
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    /**
     * Finds a user by UUID.
     *
     * @param id user identifier.
     * @return matching user when present.
     */
    public Optional<User> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
