package com.civicpulse.repository;

import com.civicpulse.model.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

    Optional<Officer> findByUsername(String username);

    boolean existsByEmployeeId(String employeeId);
}
