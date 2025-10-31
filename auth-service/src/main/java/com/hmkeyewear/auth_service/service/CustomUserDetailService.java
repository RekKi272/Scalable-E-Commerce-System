package com.hmkeyewear.auth_service.service;

import com.hmkeyewear.auth_service.model.User;
import com.hmkeyewear.auth_service.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final AuthService authService;

    @Autowired
    public CustomUserDetailService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Optional<User> user = authService.findByEmail(email);
            return user.map(CustomUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        } catch (ExecutionException | InterruptedException e) {
            throw new UsernameNotFoundException("Error fetching user from Firestore", e);
        }
    }
}
