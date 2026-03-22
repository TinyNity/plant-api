package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ili.enumeration.Role;
/**
 * Data transfer object for AddMemberRequest payloads.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    private String email;
    private Role role;

    /**
     * Builds a membership request with default {@link Role#MEMBER} role.
     *
     * @param email email address of the user to add.
     */
    public AddMemberRequest(String email) {
        this.email = email;
        this.role = Role.MEMBER;
    }
}


