package org.ili.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data transfer object for RoomResponse payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private UUID id;
    private String name;
    private UUID homeId;
}


