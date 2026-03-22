package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data transfer object for CreateHomeRequest payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHomeRequest {
    private String name;
}


