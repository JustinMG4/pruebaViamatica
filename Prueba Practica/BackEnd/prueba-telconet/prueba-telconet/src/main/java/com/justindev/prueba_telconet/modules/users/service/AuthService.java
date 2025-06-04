package com.justindev.prueba_telconet.modules.users.service;

import com.justindev.prueba_telconet.application.config.jwt.JwtBuild;
import com.justindev.prueba_telconet.application.exceptions.ClientException;
import com.justindev.prueba_telconet.modules.users.dto.AuthResponseDto;
import com.justindev.prueba_telconet.modules.users.dto.LoginDto;
import com.justindev.prueba_telconet.modules.users.dto.WhoamiDto;
import com.justindev.prueba_telconet.modules.users.repository.UserRepository;
import com.justindev.prueba_telconet.modules.users.repository.UsersHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtBuild jwtBuild;
    private final UserRepository userRepository;
    private final UsersHistoryRepository usersHistoryRepository;

    public AuthResponseDto login(LoginDto dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        Authentication auth = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String accessToken = jwtBuild.createToken(auth);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setUsername(username);
        authResponse.setMessage("User logged successfully");
        authResponse.setJwt(accessToken);
        authResponse.setStatus(true);

        return authResponse;
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
    }

    public WhoamiDto getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ClientException("User not found", HttpStatus.NOT_FOUND));
        var roles = user.getRoles().stream().map(role -> role.getAuthority().name()).toList();
        return WhoamiDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .lastname(user.getLastname())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(roles)
                .build();
    }

    public boolean isFirstLogin(String email) {
        var user = userRepository.findByPersonalEmailOrEmail(email).orElseThrow(() -> new ClientException("User not found", HttpStatus.NOT_FOUND));
        return usersHistoryRepository.countByAppUserId(user.getId()) <= 1;
    }

}


