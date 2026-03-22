package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.CareLog;

import java.util.List;
import java.util.UUID;
/**
 * Repository for {@link CareLog} persistence operations.
 */

@ApplicationScoped
public class CareLogRepository implements PanacheRepositoryBase<CareLog, UUID> {

    /**
     * Lists care logs associated with one plant.
     *
     * @param plantId plant identifier.
     * @return care logs for the plant.
     */
    public List<CareLog> findByPlantId(UUID plantId) {
        return find("plant.id", plantId).list();
    }
}

