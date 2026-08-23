package com.example.zikzak.userservice.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);
}
