package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeResponse {
    private Long id;
    private String name;
    private List<String> memberUsernames;
}
