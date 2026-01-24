package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantResponse {
    private Long id;
    private String name;
    private String species;
    private Integer wateringFrequency;
    private LocalDate lastWateredDate;
    private Long roomId;
    private String roomName;
}
