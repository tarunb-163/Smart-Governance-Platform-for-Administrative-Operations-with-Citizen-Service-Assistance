package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.ComplaintAttachment;
import com.civicpulse.repository.ComplaintAttachmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintAttachmentService {

    private final ComplaintAttachmentRepository attachmentRepository;

    public ComplaintAttachmentService(ComplaintAttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<ComplaintAttachment> getAttachmentsForComplaint(Complaint complaint) {
        return attachmentRepository.findByComplaint(complaint);
    }

    public ComplaintAttachment save(ComplaintAttachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public void delete(Long id) {
        attachmentRepository.deleteById(id);
    }
}
