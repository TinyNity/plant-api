package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlantRequest {
    private String name;
    private String species;
    private Integer wateringFrequency;
    private LocalDate lastWateredDate;
    private Long roomId;
}
