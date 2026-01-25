package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlantRequest {
    private String name;
    private String species;
    private Integer wateringFrequency;
    private LocalDate lastWateredDate;
    private Long roomId;
}
