package org.ili.dto.dev;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data transfer object for SeededUserCredential payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeededUserCredential {

    private String username;
    private String email;
}


