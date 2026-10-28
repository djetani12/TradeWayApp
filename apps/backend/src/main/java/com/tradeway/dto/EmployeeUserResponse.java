package com.tradeway.dto;

import com.tradeway.model.UserAccount;

public record EmployeeUserResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String status,
    java.util.Set<String> roles
) {
    public static EmployeeUserResponse from(UserAccount user) {
        return new EmployeeUserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getStatus().name(),
            user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
        );
    }
}
