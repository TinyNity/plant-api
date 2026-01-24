package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Plant;

import java.util.List;

@ApplicationScoped
public class PlantRepository implements PanacheRepository<Plant> {
    public List<Plant> findByRoomId(Long roomId) {
        return find("room.id", roomId).list();
    }
}
