package ggm.board.domain.auth.service;

import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Optional<Auth> memberOptional = authRepository.findByEmail(name);

        Auth auth = memberOptional.orElseThrow(() ->
            new UsernameNotFoundException("User not found")
        );

        return new CustomUserDetails(auth);
    }
}
