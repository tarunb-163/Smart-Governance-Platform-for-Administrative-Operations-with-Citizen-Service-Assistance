package com.civicpulse.repository;

import com.civicpulse.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByEmail(String email);

    Optional<Citizen> findByEmailIgnoreCase(String email);

    Optional<Citizen> findByCitizenId(String citizenId);

    Optional<Citizen> findByCitizenIdIgnoreCase(String citizenId);

    Optional<Citizen> findByUsername(String username);

    Optional<Citizen> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCitizenId(String citizenId);

    boolean existsByCitizenIdIgnoreCase(String citizenId);

    boolean existsByUsernameIgnoreCase(String username);

    List<Citizen> findAllByOrderByCreatedAtDesc();

    long countByActiveTrue();

    long countByActiveFalse();
}
