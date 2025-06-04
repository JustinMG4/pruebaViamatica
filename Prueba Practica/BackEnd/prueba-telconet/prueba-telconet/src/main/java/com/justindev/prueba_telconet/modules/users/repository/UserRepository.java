package com.justindev.prueba_telconet.modules.users.repository;

import com.justindev.prueba_telconet.modules.users.model.AppUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u WHERE u.username = :username OR u.email = :username")
    Optional<AppUser> findByUsernameOrEmail(String username);


    @Query("SELECT u FROM AppUser u WHERE u.personalEmail = :email OR u.email = :email")
    Optional<AppUser> findByPersonalEmailOrEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPersonalEmail(String personalEmail);

    boolean existsByUsername(String username);

    Optional <AppUser> findByUsername(String username);

    Optional <AppUser> findByEmail(String email);

    boolean existsByIdentification(String identification);
}
