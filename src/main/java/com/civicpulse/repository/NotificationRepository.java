package com.civicpulse.repository;

import com.civicpulse.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCitizenEmailOrderByCreatedAtDesc(String citizenEmail);

    List<Notification> findTop20ByCitizenEmailOrderByCreatedAtDesc(String citizenEmail);

    long countByCitizenEmailAndIsReadFalse(String citizenEmail);
}
