package org.ili.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    public String species;

    @Column(name = "watering_frequency")
    public Integer wateringFrequency; // en jours

    @Column(name = "last_watered_date")
    public LocalDate lastWateredDate;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    public Room room;
}
