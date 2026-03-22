package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data transfer object for CreateRoomRequest payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String name;
}


