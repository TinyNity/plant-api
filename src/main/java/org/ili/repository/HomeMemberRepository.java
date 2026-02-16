package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeMemberRepository implements PanacheRepositoryBase<HomeMember, HomeMemberId> {

    public List<HomeMember> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    public List<HomeMember> findByHomeId(UUID homeId) {
        return find("home.id", homeId).list();
    }
}
