package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantResponse {

    private UUID id;
    private String name;
    private String species;
    private Integer wateringFrequency;
    private LocalDate lastWateredDate;

    private String photoUrl;
    private LocalDate pottedDate;
    private Boolean deceased;

    private LocalDate nextWateringDate;
    private Boolean needsWatering;

    private UUID roomId;
    private String roomName;

    private UUID homeId;
}
