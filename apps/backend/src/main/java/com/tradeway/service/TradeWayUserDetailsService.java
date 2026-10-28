package com.tradeway.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tradeway.model.AccountStatus;
import com.tradeway.model.UserAccount;
import com.tradeway.repository.UserAccountRepository;

@Service
public class TradeWayUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public TradeWayUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(username)
            .or(() -> userAccountRepository.findByEmailIgnoreCase(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        boolean active = user.isEnabled()
            && !user.isLocked()
            && user.getStatus() != AccountStatus.SUSPENDED
            && user.getStatus() != AccountStatus.RESTRICTED;

        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));

        return new User(
            user.getUsername(),
            user.getPasswordHash(),
            active,
            true,
            true,
            !user.isLocked() && user.getStatus() != AccountStatus.SUSPENDED && user.getStatus() != AccountStatus.RESTRICTED,
            authorities
        );
    }
}
