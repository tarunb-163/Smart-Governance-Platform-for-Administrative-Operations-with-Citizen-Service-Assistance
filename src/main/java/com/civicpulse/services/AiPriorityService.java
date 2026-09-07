package com.civicpulse.services;

import org.springframework.stereotype.Service;

@Service
public class AiPriorityService {

    public PriorityResult predictPriority(
            String title,
            String description,
            String category,
            String location) {

        String text = (
                safe(title) + " " +
                        safe(description) + " " +
                        safe(category) + " " +
                        safe(location)
        ).toLowerCase();

        int score = 0;

        // =====================================================
        // CRITICAL KEYWORDS
        // =====================================================
        String[] criticalKeywords = {
                "fire",
                "accident",
                "death",
                "dead",
                "life threatening",
                "life-threatening",
                "explosion",
                "gas leak",
                "electric shock",
                "electrocution",
                "building collapse",
                "collapsed building",
                "flood",
                "major flood",
                "emergency"
        };

        for (String keyword : criticalKeywords) {
            if (text.contains(keyword)) {
                score += 40;
            }
        }

        // =====================================================
        // HIGH PRIORITY KEYWORDS
        // =====================================================
        String[] highKeywords = {
                "danger",
                "dangerous",
                "unsafe",
                "severe",
                "urgent",
                "broken electric pole",
                "fallen pole",
                "open manhole",
                "sewage overflow",
                "water leakage",
                "no water",
                "power outage",
                "street light not working",
                "road accident",
                "large pothole",
                "major pothole",
                "contamination"
        };

        for (String keyword : highKeywords) {
            if (text.contains(keyword)) {
                score += 20;
            }
        }

        // =====================================================
        // MEDIUM PRIORITY KEYWORDS
        // =====================================================
        String[] mediumKeywords = {
                "pothole",
                "garbage",
                "waste",
                "drain",
                "drainage",
                "street light",
                "water",
                "road damage",
                "sanitation",
                "noise",
                "parking"
        };

        for (String keyword : mediumKeywords) {
            if (text.contains(keyword)) {
                score += 8;
            }
        }

        // =====================================================
        // CATEGORY BASED SCORING
        // =====================================================
        if (category != null) {

            String cat = category.toLowerCase();

            if (cat.contains("electricity")) {
                score += 15;
            }

            if (cat.contains("water")) {
                score += 12;
            }

            if (cat.contains("sanitation")) {
                score += 12;
            }

            if (cat.contains("road")) {
                score += 8;
            }

            if (cat.contains("fire")) {
                score += 40;
            }
        }

        // =====================================================
        // LOCATION BASED RISK
        // =====================================================
        if (location != null) {

            String loc = location.toLowerCase();

            if (loc.contains("school")
                    || loc.contains("hospital")
                    || loc.contains("market")
                    || loc.contains("bus stop")
                    || loc.contains("railway")) {

                score += 10;
            }
        }

        // =====================================================
        // DESCRIPTION LENGTH / DETAIL
        // =====================================================
        if (description != null) {

            if (description.length() > 200) {
                score += 5;
            }

            if (description.length() > 500) {
                score += 5;
            }
        }

        // =====================================================
        // FINAL PRIORITY
        // =====================================================
        String priority;
        String reason;
        int confidence;

        if (score >= 60) {

            priority = "CRITICAL";

            reason = "The complaint contains indicators of an immediate public safety or emergency risk.";

            confidence = Math.min(98, 85 + score / 10);

        } else if (score >= 30) {

            priority = "HIGH";

            reason = "The complaint contains significant risk indicators and may require quick administrative attention.";

            confidence = Math.min(95, 78 + score / 10);

        } else if (score >= 12) {

            priority = "MEDIUM";

            reason = "The complaint appears to require normal administrative attention but does not indicate an immediate emergency.";

            confidence = Math.min(92, 72 + score / 10);

        } else {

            priority = "LOW";

            reason = "No major urgency or public safety risk indicators were detected.";

            confidence = 75;
        }

        return new PriorityResult(
                priority,
                reason,
                confidence
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // =========================================================
    // RESULT CLASS
    // =========================================================

    public static class PriorityResult {

        private final String priority;
        private final String reason;
        private final int confidence;

        public PriorityResult(
                String priority,
                String reason,
                int confidence) {

            this.priority = priority;
            this.reason = reason;
            this.confidence = confidence;
        }

        public String getPriority() {
            return priority;
        }

        public String getReason() {
            return reason;
        }

        public int getConfidence() {
            return confidence;
        }
    }
}