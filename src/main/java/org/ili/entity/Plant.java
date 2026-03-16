package org.ili.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
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

    @Column(name = "photo_url")
    public String photoUrl;

    @Column(name = "potted_date")
    public LocalDate pottedDate;

    @Column(name = "is_deceased", nullable = false)
    @Builder.Default
    public Boolean deceased = false;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    public Room room;
}