package org.ili.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;

import java.util.List;
import java.util.UUID;
/**
 * Repository for {@link HomeMember} persistence operations.
 */

@ApplicationScoped
public class HomeMemberRepository implements PanacheRepositoryBase<HomeMember, HomeMemberId> {

    /**
     * Lists memberships associated with one user.
     *
     * @param userId user identifier.
     * @return memberships for the user.
     */
    public List<HomeMember> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    /**
     * Lists memberships associated with one home.
     *
     * @param homeId home identifier.
     * @return memberships for the home.
     */
    public List<HomeMember> findByHomeId(UUID homeId) {
        return find("home.id", homeId).list();
    }
}

