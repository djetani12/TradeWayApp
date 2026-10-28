package com.tradeway.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeReportingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analystCanAccessReportingDashboard() throws Exception {
        mockMvc.perform(get("/api/employee/dashboard")
                .with(user(new User("analyst", "secret", java.util.List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANALYST"))))))
            .andExpect(status().isOk());
    }

    @Test
    void customerCannotAccessReportingDashboard() throws Exception {
        mockMvc.perform(get("/api/employee/dashboard")
                .with(user(new User("customer", "secret", java.util.List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER"))))))
            .andExpect(status().isForbidden());
    }
}
