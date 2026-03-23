package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndBlacklistedFalse(username);

        if (user == null) {
            throw new UsernameNotFoundException("User with username not exists: " + username);
        }

        return new SecurityUser(user);
    }
}
