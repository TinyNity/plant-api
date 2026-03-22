package org.ili.dto.dev;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Data transfer object for DevSeedResponse payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevSeedResponse {

    private boolean replacedExisting;
    private int userCount;
    private int homesCreated;
    private int membershipsCreated;
    private int roomsCreated;
    private int plantsCreated;
    private int careLogsCreated;
    private String defaultPassword;
    private List<SeededUserCredential> sampleUsers;
}


