package org.ili.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.ili.entity.CareLog;
/**
 * Data transfer object for CareLogResponse payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareLogResponse {
        private UUID id;
        private CareLog.CareType type;
        private String note;
        private LocalDateTime date;
}


