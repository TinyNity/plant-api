package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Plant;

import java.util.List;
import java.util.UUID;
/**
 * Repository for {@link Plant} persistence operations.
 */

@ApplicationScoped
public class PlantRepository implements PanacheRepositoryBase<Plant, UUID> {

    /**
     * Lists plants belonging to a room.
     *
     * @param roomId room identifier.
     * @return plants associated with the room.
     */
    public List<Plant> findByRoomId(UUID roomId) {
        return find("room.id", roomId).list();
    }
}

