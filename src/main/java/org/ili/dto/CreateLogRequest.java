package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ili.entity.CareLog;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLogRequest {
    private CareLog.CareType type;
    private String note;
}
