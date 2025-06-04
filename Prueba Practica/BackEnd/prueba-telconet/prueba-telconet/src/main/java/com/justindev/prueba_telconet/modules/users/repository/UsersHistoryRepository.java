package com.justindev.prueba_telconet.modules.users.repository;

import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.model.UsersHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersHistoryRepository extends JpaRepository<UsersHistory, Long> {

    @Query("SELECT COUNT (u) FROM UsersHistory u JOIN u.appUser user WHERE user.id = :userId")
    Long countByAppUserId(Long userId);

    List<UsersHistory> findAllByAppUserId(Long userId);

}
