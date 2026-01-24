package org.ili.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "home_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeMember extends PanacheEntityBase {

    @EmbeddedId
    public HomeMemberId id;

    @ManyToOne
    @MapsId("homeId")
    @JoinColumn(name = "home_id")
    public Home home;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;

    public enum Role {
        OWNER, GUEST, ADMIN
    }
}
