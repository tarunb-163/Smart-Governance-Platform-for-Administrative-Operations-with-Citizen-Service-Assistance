package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRoutingService departmentRoutingService;

    // Common English stop words to filter out when comparing descriptions
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "in", "on", "at", "to", "for", "from", "of", "by", "with", "about",
            "and", "or", "but", "so", "if", "there", "this", "that", "these", "those",
            "my", "our", "your", "their", "we", "they", "i", "it", "its", "has", "have", "had",
            "do", "does", "did", "not", "please", "kindly", "issue", "problem", "complaint", "near"
    );

    public DuplicateDetectionService(ComplaintRepository complaintRepository, DepartmentRoutingService departmentRoutingService) {
        this.complaintRepository = complaintRepository;
        this.departmentRoutingService = departmentRoutingService;
    }

    public static class DuplicateCheckResult {
        private final boolean duplicate;
        private final String existingComplaintNumber;
        private final String existingStatus;
        private final String existingTitle;
        private final String existingCategory;
        private final String existingLocation;
        private final String message;

        public DuplicateCheckResult(boolean duplicate, String existingComplaintNumber, String existingStatus,
                                    String existingTitle, String existingCategory, String existingLocation, String message) {
            this.duplicate = duplicate;
            this.existingComplaintNumber = existingComplaintNumber;
            this.existingStatus = existingStatus;
            this.existingTitle = existingTitle;
            this.existingCategory = existingCategory;
            this.existingLocation = existingLocation;
            this.message = message;
        }

        public static DuplicateCheckResult notDuplicate() {
            return new DuplicateCheckResult(false, null, null, null, null, null, null);
        }

        public static DuplicateCheckResult duplicateFound(Complaint existing, String reason) {
            return new DuplicateCheckResult(
                    true,
                    existing.getTrackingId(),
                    existing.getStatus(),
                    existing.getTitle(),
                    existing.getCategory(),
                    existing.getLocation(),
                    reason
            );
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getExistingComplaintNumber() {
            return existingComplaintNumber;
        }

        public String getExistingStatus() {
            return existingStatus;
        }

        public String getExistingTitle() {
            return existingTitle;
        }

        public String getExistingCategory() {
            return existingCategory;
        }

        public String getExistingLocation() {
            return existingLocation;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Checks if an active similar complaint already exists.
     */
    public DuplicateCheckResult checkDuplicate(String category, String title, String description, String location) {
        if (description == null || description.trim().isEmpty() || location == null || location.trim().isEmpty()) {
            return DuplicateCheckResult.notDuplicate();
        }

        String incomingDept = departmentRoutingService.determineDepartment(category, title, description);
        List<Complaint> activeComplaints = complaintRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && !c.getStatus().equalsIgnoreCase("Resolved") && !c.getStatus().equalsIgnoreCase("Rejected"))
                .collect(Collectors.toList());

        for (Complaint existing : activeComplaints) {
            String existingDept = existing.getDepartment() != null ? existing.getDepartment()
                    : departmentRoutingService.determineDepartment(existing.getCategory(), existing.getTitle(), existing.getDescription());

            // 1. Department / Category must be compatible
            boolean deptMatches = incomingDept.equalsIgnoreCase(existingDept);
            boolean categoryMatches = category != null && existing.getCategory() != null
                    && category.trim().equalsIgnoreCase(existing.getCategory().trim());

            if (!deptMatches && !categoryMatches) {
                continue;
            }

            // 2. Location Similarity
            double locSimilarity = calculateSimilarity(location, existing.getLocation());
            boolean locationMatches = locSimilarity >= 0.5 ||
                    containsIgnoreCase(location, existing.getLocation()) ||
                    containsIgnoreCase(existing.getLocation(), location);

            if (!locationMatches) {
                continue;
            }

            // 3. Text Similarity between descriptions & titles
            String incomingText = (title != null ? title : "") + " " + description;
            String existingText = (existing.getTitle() != null ? existing.getTitle() : "") + " " + (existing.getDescription() != null ? existing.getDescription() : "");
            double textSimilarity = calculateSimilarity(incomingText, existingText);

            // If location matches well and text similarity is above threshold (e.g., 0.35 Jaccard overlap on significant tokens)
            if (textSimilarity >= 0.30) {
                String reason = String.format("A similar active complaint (%s) is already logged for %s in %s department.",
                        existing.getTrackingId(), existing.getLocation(), existingDept);
                return DuplicateCheckResult.duplicateFound(existing, reason);
            }
        }

        return DuplicateCheckResult.notDuplicate();
    }

    /**
     * Computes token Jaccard similarity between two strings, ignoring case, punctuation, and common stop words.
     */
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }

        Set<String> tokens1 = tokenize(text1);
        Set<String> tokens2 = tokenize(text2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = cleaned.split("\\s+");
        Set<String> result = new HashSet<>();
        for (String w : words) {
            w = w.trim();
            if (w.length() > 2 && !STOP_WORDS.contains(w)) {
                if (w.endsWith("ing") && w.length() > 5) {
                    w = w.substring(0, w.length() - 3);
                } else if (w.endsWith("es") && w.length() > 4) {
                    w = w.substring(0, w.length() - 2);
                } else if (w.endsWith("s") && w.length() > 3) {
                    w = w.substring(0, w.length() - 1);
                }
                result.add(w);
            }
        }
        return result;
    }

    private boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null || searchStr.trim().isEmpty()) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.trim().toLowerCase());
    }
}
