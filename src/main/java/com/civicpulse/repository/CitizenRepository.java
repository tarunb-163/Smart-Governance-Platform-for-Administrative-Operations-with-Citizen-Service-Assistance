package com.civicpulse.repository;

import com.civicpulse.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByEmail(String email);

    Optional<Citizen> findByCitizenId(String citizenId);

    boolean existsByEmail(String email);

    boolean existsByCitizenId(String citizenId);
}
