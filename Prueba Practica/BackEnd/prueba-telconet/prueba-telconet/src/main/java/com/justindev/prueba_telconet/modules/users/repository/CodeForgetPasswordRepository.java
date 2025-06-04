package com.justindev.prueba_telconet.modules.users.repository;

import com.justindev.prueba_telconet.modules.users.model.CodeForgetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeForgetPasswordRepository extends JpaRepository<CodeForgetPassword, UUID> {
    boolean existsByCode(String code);

    @Query("SELECT c FROM CodeForgetPassword c WHERE c.code = :code AND c.user.email = :email")
    Optional<CodeForgetPassword> findByCodeAndUserEmail(String code, String email);
}
