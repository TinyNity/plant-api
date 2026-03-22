package org.ili.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
/**
 * JPA entity representing CareLog in the domain model.
 */

@Entity
@Table(name = "care_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)

public class CareLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CareType type;

    public String note;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    public Plant plant;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    public enum CareType {
        WATERING, FERTILIZING, PRUNING, MISTING, OTHER
    }
}


