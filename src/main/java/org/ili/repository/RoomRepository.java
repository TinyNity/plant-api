package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Room;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RoomRepository implements PanacheRepositoryBase<Room, UUID> {

    public List<Room> findByHomeId(UUID homeId) {
        return find("home.id", homeId).list();
    }
}
