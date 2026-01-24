package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Room;

import java.util.List;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {
    public List<Room> findByHomeId(Long homeId) {
        return find("home.id", homeId).list();
    }
}
