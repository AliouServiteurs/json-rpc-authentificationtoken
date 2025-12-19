package com.leseviteurs.json_rpc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leseviteurs.json_rpc.model.UserAccount;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}
