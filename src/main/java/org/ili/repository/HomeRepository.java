package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.Home;

@ApplicationScoped
public class HomeRepository implements PanacheRepository<Home> {
}
