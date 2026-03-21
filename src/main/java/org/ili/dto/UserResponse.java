package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.ili.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;

    public static UserResponse from(User user) {
        UserResponse res = new UserResponse();
        res.id = user.id;
        res.email = user.email;
        res.username = user.username;
        return res;
    }
}
