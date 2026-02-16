package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.CareLog;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CareLogRepository implements PanacheRepositoryBase<CareLog, UUID> {

    public List<CareLog> findByPlantId(UUID plantId) {
        return find("plant.id", plantId).list();
    }
}
