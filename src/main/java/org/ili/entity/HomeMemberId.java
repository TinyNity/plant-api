package org.ili.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeMemberId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID homeId;
    private UUID userId;
}
