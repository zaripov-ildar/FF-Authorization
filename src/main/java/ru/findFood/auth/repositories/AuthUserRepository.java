package ru.findFood.auth.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.entities.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long>, JpaSpecificationExecutor<AuthUser> {
    Optional<AuthUser> findByEmail(String userEmail);

    boolean existsByEmail(String email);


    void deleteByEmail(String email);




}
