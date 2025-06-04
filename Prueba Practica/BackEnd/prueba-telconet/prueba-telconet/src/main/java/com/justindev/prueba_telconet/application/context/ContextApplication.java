package com.justindev.prueba_telconet.application.context;

import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.model.Authorities;
import com.justindev.prueba_telconet.modules.users.model.enums.UserRoles;
import com.justindev.prueba_telconet.modules.users.repository.AuthorityRepository;
import com.justindev.prueba_telconet.modules.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Log4j2
@Configuration
@Transactional
@RequiredArgsConstructor
public class ContextApplication {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.admin.email}")
    private String emailAdmin;

    @Value("${user.admin.password}")
    private String passwordAdmin;

    private final List<UserRoles> roles = Arrays.asList(UserRoles.values());

    @PostConstruct
    public void init() {
        tryLoadRoles();
        tryLoadUserAdmin();
    }

    private void tryLoadRoles() {
        log.info("Trying to load roles");
        roles.forEach(role -> {
            if (!authorityRepository.existsByAuthority(role)) {
                authorityRepository.save(Authorities.builder().authority(role).build());
            }
        });
    }

    private void tryLoadUserAdmin() {
        log.info("Trying to load user admin");
        if (!userRepository.existsByEmail(emailAdmin)) {

            var userRole = authorityRepository.findByAuthority(UserRoles.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            userRepository.save(AppUser.builder()
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .name("admin")
                    .address("Lorem ipsum")
                    .phone("0000000000")
                    .lastname("admin")
                    .email(emailAdmin)
                    .username(emailAdmin)
                    .identification("0000000000")
                    .personalEmail("justinn@mail.com")
                    .password(passwordEncoder.encode(passwordAdmin))
                    .roles(Set.of(userRole))
                    .build());

        }

    }
}
