package com.justindev.prueba_telconet.modules.users.service;

import com.justindev.prueba_telconet.application.config.jwt.JwtBuild;
import com.justindev.prueba_telconet.application.exceptions.ClientException;
import com.justindev.prueba_telconet.modules.users.dto.AuthResponseDto;
import com.justindev.prueba_telconet.modules.users.dto.LoginDto;
import com.justindev.prueba_telconet.modules.users.dto.WhoAmIDto;
import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.model.UsersHistory;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtBuild jwtBuild;
    private final UserRepository userRepository;
    private final UsersHistoryRepository usersHistoryRepository;
    private static final String USER_NOT_FOUND = "User not found";

    public AuthResponseDto login(LoginDto dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        Authentication auth = this.authenticate(username, password);

        var user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        verifyUniqueSession(user);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String accessToken = jwtBuild.createToken(auth);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setUsername(username);
        authResponse.setMessage("User logged successfully");
        authResponse.setJwt(accessToken);
        authResponse.setStatus(true);


        buildUserHistory(user);

        return authResponse;
    }

    private void buildUserHistory(AppUser user) {
        var userHistory = UsersHistory.builder()
                .appUser(user)
                .loginDate(LocalDateTime.now())
                .active(true)
                .build();
        usersHistoryRepository.save(userHistory);
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

    private void verifyUniqueSession(AppUser user) {
        var userHistory = usersHistoryRepository.findByAppUserAndActive(user, true);
        if (userHistory != null) {
            throw new ClientException("User already logged in", HttpStatus.CONFLICT);
        }
    }

    public WhoAmIDto getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var roles = user.getRoles().stream().map(role -> role.getAuthority().name()).toList();
        return WhoAmIDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .identification(user.getIdentification())
                .personalEmail(user.getPersonalEmail())
                .lastname(user.getLastname())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(roles)
                .build();
    }

    public boolean isFirstLogin(String email) {
        var user = userRepository.findByPersonalEmailOrEmail(email).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return usersHistoryRepository.countByAppUserId(user.getId()) <= 1;
    }

    public void registerLogout(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var userHistory = usersHistoryRepository.findTopByAppUser(user);
        if (userHistory != null) {
            userHistory.setLogoutDate(LocalDateTime.now());
            userHistory.setActive(false);
            usersHistoryRepository.save(userHistory);
        } else {
            throw new ClientException("User history not found", HttpStatus.NOT_FOUND);
        }

    }

}


