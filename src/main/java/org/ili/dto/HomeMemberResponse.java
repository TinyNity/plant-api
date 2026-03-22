package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.ili.enumeration.Role;
/**
 * Data transfer object for HomeMemberResponse payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeMemberResponse {

    private UUID userId;
    private String username;
    private Role role;
}


