package com.tradeway.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;

public record UserProfileResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String status,
    Set<String> roles
) {

    public static UserProfileResponse from(UserAccount userAccount) {
        return new UserProfileResponse(
            userAccount.getId(),
            userAccount.getUsername(),
            userAccount.getEmail(),
            userAccount.getFirstName(),
            userAccount.getLastName(),
            userAccount.getStatus().name(),
            userAccount.getRoles().stream().map(UserRole::name).collect(Collectors.toSet())
        );
    }
}
