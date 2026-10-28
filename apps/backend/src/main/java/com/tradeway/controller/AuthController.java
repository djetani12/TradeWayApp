package com.tradeway.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tradeway.dto.AuthResponse;
import com.tradeway.dto.LoginRequest;
import com.tradeway.dto.RegisterRequest;
import com.tradeway.dto.UserProfileResponse;
import com.tradeway.model.AccountLedgerEntry;
import com.tradeway.model.AccountStatus;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;
import com.tradeway.repository.AccountLedgerEntryRepository;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.UserAccountRepository;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final AccountLedgerEntryRepository accountLedgerEntryRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserAccountRepository userAccountRepository,
                          CustomerAccountRepository customerAccountRepository,
                          AccountLedgerEntryRepository accountLedgerEntryRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.accountLedgerEntryRepository = accountLedgerEntryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of(
            "headerName", csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName(),
            "token", csrfToken.getToken()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                HttpServletRequest request) {
        String normalizedUsername = registerRequest.username().trim();
        String normalizedEmail = registerRequest.email().trim();
        String normalizedFirstName = registerRequest.firstName().trim();
        String normalizedLastName = registerRequest.lastName().trim();

        if (userAccountRepository.findByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userAccountRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccount user = new UserAccount(
            normalizedUsername,
            normalizedEmail,
            passwordEncoder.encode(registerRequest.password()),
            normalizedFirstName,
            normalizedLastName,
            EnumSet.of(UserRole.CUSTOMER),
            AccountStatus.ACTIVE
        );
        user = userAccountRepository.save(user);

        String accountNumber = nextAccountNumber();
        CustomerAccount account = new CustomerAccount(
            accountNumber,
            user,
            AccountStatus.ACTIVE,
            BigDecimal.valueOf(25000.00),
            BigDecimal.valueOf(25000.00)
        );
        account = customerAccountRepository.save(account);

        accountLedgerEntryRepository.saveAll(List.of(
            new AccountLedgerEntry(account, "CREDIT", BigDecimal.valueOf(25000.00), "Initial funding"),
            new AccountLedgerEntry(account, "CREDIT", BigDecimal.valueOf(1500.00), "Welcome bonus")
        ));

        issueAuthenticationSession(normalizedUsername, registerRequest.password(), request);
        user.setLastLoginAt(Instant.now());
        userAccountRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            issueAuthenticationSession(loginRequest.username(), loginRequest.password(), request);

            UserAccount user = userAccountRepository.findByUsernameIgnoreCase(loginRequest.username())
                .or(() -> userAccountRepository.findByEmailIgnoreCase(loginRequest.username()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
            user.setLastLoginAt(Instant.now());
            userAccountRepository.save(user);

            return ResponseEntity.ok(AuthResponse.from(user));
        } catch (Exception ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password", ex);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(authentication.getName())
            .or(() -> userAccountRepository.findByEmailIgnoreCase(authentication.getName()))
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));

        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Signed out successfully"));
    }

    private void issueAuthenticationSession(String username, String password, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private String nextAccountNumber() {
        long suffix = System.currentTimeMillis() % 900000L + 100000L;
        String candidate = "TRW-" + suffix;
        while (customerAccountRepository.findByAccountNumber(candidate).isPresent()) {
            suffix = (suffix + 1) % 900000L + 100000L;
            candidate = "TRW-" + suffix;
        }
        return candidate;
    }
}
