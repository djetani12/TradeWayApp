package com.tradeway.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;

public record AuthResponse(
    String username,
    String firstName,
    String lastName,
    Set<String> roles,
    String status
) {

    public static AuthResponse from(UserAccount userAccount) {
        return new AuthResponse(
            userAccount.getUsername(),
            userAccount.getFirstName(),
            userAccount.getLastName(),
            userAccount.getRoles().stream().map(UserRole::name).collect(Collectors.toSet()),
            userAccount.getStatus().name()
        );
    }
}
