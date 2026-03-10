package org.ili.dto.dev;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevSeedRequest {

    private Integer userCount;
    private Integer homesPerUser;
    private Integer additionalMembersPerHome;
    private Integer roomsPerHome;
    private Integer plantsPerRoom;
    private Integer logsPerPlant;
    private Boolean replaceExisting;
}
