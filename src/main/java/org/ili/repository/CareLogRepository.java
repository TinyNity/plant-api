package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.CareLog;

import java.util.List;

@ApplicationScoped
public class CareLogRepository implements PanacheRepository<CareLog> {
    public List<CareLog> findByPlantId(Long plantId) {
        return find("plant.id", plantId).list();
    }
}
