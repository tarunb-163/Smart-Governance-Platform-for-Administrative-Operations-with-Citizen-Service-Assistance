package com.civicpulse.repository;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Lookup by Complaint Number (e.g. CMP202600124)
    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    // Lookups by Officer assignment
    List<Complaint> findByAssignedOfficer(Officer officer);

    List<Complaint> findByAssignedOfficerAndStatus(Officer officer, String status);

    List<Complaint> findByAssignedOfficerAndPriority(Officer officer, String priority);

    // Filter complaints by department
    List<Complaint> findByDepartment(String department);

    List<Complaint> findByDepartmentAndStatus(String department, String status);

    // Fetch join queries
    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.attachments WHERE c.id = :id")
    Optional<Complaint> findByIdWithAttachments(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.attachments WHERE c.complaintNumber = :complaintNumber")
    Optional<Complaint> findByComplaintNumberWithAttachments(@Param("complaintNumber") String complaintNumber);
}