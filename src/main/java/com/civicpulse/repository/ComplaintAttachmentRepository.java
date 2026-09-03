package com.civicpulse.repository;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, Long> {

    List<ComplaintAttachment> findByComplaint(Complaint complaint);
}
