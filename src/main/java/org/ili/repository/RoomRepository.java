package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Room;

import java.util.List;
import java.util.UUID;
/**
 * Repository for {@link Room} persistence operations.
 */

@ApplicationScoped
public class RoomRepository implements PanacheRepositoryBase<Room, UUID> {

    /**
     * Lists rooms by home identifier.
     *
     * @param homeId home identifier.
     * @return rooms associated with the home.
     */
    public List<Room> findByHomeId(UUID homeId) {
        return find("home.id", homeId).list();
    }
}

