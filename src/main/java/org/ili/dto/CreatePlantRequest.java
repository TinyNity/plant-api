package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlantRequest {

    private String name;
    private String species;
    private String nickname;
    private Integer wateringFrequency;
    private LocalDate lastWateredDate;
    private String photoUrl;
    private LocalDate pottedDate;
    private Boolean deceased;
    private UUID roomId;

    public CreatePlantRequest(String name, String species, Integer wateringFrequency, LocalDate lastWateredDate, UUID roomId) {
        this.name = name;
        this.species = species;
        this.wateringFrequency = wateringFrequency;
        this.lastWateredDate = lastWateredDate;
        this.roomId = roomId;
        this.deceased = false;
    }
}
