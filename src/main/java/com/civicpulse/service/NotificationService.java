package com.civicpulse.service;

import com.civicpulse.model.Notification;
import com.civicpulse.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(String citizenEmail, String title, String message, String complaintNumber, String type) {
        if (citizenEmail == null || citizenEmail.trim().isEmpty()) {
            return null;
        }
        Notification notification = new Notification(citizenEmail.trim().toLowerCase(), title, message, complaintNumber, type);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForCitizen(String citizenEmail) {
        if (citizenEmail == null || citizenEmail.trim().isEmpty()) {
            return List.of();
        }
        return notificationRepository.findByCitizenEmailOrderByCreatedAtDesc(citizenEmail.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String citizenEmail) {
        if (citizenEmail == null || citizenEmail.trim().isEmpty()) {
            return 0;
        }
        return notificationRepository.countByCitizenEmailAndIsReadFalse(citizenEmail.trim().toLowerCase());
    }

    @Transactional
    public void markAllAsRead(String citizenEmail) {
        if (citizenEmail == null || citizenEmail.trim().isEmpty()) {
            return;
        }
        List<Notification> list = notificationRepository.findByCitizenEmailOrderByCreatedAtDesc(citizenEmail.trim().toLowerCase());
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }

    @Transactional
    public Notification createOfficerNotification(String officerUsername, String title, String message, String complaintNumber, String type) {
        if (officerUsername == null || officerUsername.trim().isEmpty()) {
            return null;
        }
        Notification notification = Notification.forOfficer(officerUsername.trim(), title, message, complaintNumber, type);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForOfficer(String officerUsername) {
        if (officerUsername == null || officerUsername.trim().isEmpty()) {
            return List.of();
        }
        return notificationRepository.findByOfficerUsernameOrderByCreatedAtDesc(officerUsername.trim());
    }

    @Transactional(readOnly = true)
    public long getUnreadCountForOfficer(String officerUsername) {
        if (officerUsername == null || officerUsername.trim().isEmpty()) {
            return 0;
        }
        return notificationRepository.countByOfficerUsernameAndIsReadFalse(officerUsername.trim());
    }

    @Transactional
    public void markAllAsReadForOfficer(String officerUsername) {
        if (officerUsername == null || officerUsername.trim().isEmpty()) {
            return;
        }
        List<Notification> list = notificationRepository.findByOfficerUsernameOrderByCreatedAtDesc(officerUsername.trim());
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }

    @Transactional
    public boolean markAsReadForOfficer(Long notificationId, String officerUsername) {
        if (notificationId == null || officerUsername == null) {
            return false;
        }
        return notificationRepository.findById(notificationId).map(n -> {
            if (officerUsername.equalsIgnoreCase(n.getOfficerUsername())) {
                n.setRead(true);
                notificationRepository.save(n);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
