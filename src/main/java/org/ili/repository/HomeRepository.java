package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Home;

import java.util.UUID;

@ApplicationScoped
public class HomeRepository implements PanacheRepositoryBase<Home, UUID> {
}
