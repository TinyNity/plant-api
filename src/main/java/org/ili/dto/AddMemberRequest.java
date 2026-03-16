package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ili.enumeration.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    private String email;
    private Role role;

    public AddMemberRequest(String email) {
        this.email = email;
        this.role = Role.MEMBER;
    }
}