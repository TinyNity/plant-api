package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Plant;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PlantRepository implements PanacheRepositoryBase<Plant, UUID> {

    public List<Plant> findByRoomId(UUID roomId) {
        return find("room.id", roomId).list();
    }
}
