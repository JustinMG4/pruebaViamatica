package com.justindev.prueba_telconet.modules.users.repository;

import com.justindev.prueba_telconet.modules.users.model.Authorities;
import com.justindev.prueba_telconet.modules.users.model.enums.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authorities, Long> {

   Optional <Authorities> findByAuthority(UserRoles name);

    boolean existsByAuthority(UserRoles role);
}
