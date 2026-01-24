package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;

import java.util.List;

@ApplicationScoped
public class HomeMemberRepository implements PanacheRepository<HomeMember> {
    public List<HomeMember> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }

    public List<HomeMember> findByHomeId(Long homeId) {
        return find("home.id", homeId).list();
    }
}
