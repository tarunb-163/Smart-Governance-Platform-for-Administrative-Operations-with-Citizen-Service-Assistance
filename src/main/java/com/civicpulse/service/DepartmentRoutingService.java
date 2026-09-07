package com.civicpulse.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepartmentRoutingService {

    public static final String DEPT_ROADS = "Roads / Public Works Department";
    public static final String DEPT_WATER = "Water Department";
    public static final String DEPT_SANITATION = "Sanitation Department";
    public static final String DEPT_ELECTRICITY = "Electricity Department";
    public static final String DEPT_DRAINAGE = "Public Works / Sanitation Department";
    public static final String DEPT_PUBLIC_SAFETY = "Public Safety Department";
    public static final String DEPT_GENERAL = "General Administration";

    private static final List<String> ALL_DEPARTMENTS = List.of(
            DEPT_ROADS,
            DEPT_WATER,
            DEPT_SANITATION,
            DEPT_ELECTRICITY,
            DEPT_DRAINAGE,
            DEPT_PUBLIC_SAFETY,
            DEPT_GENERAL
    );

    /**
     * Determines the responsible department based on category and textual cues.
     */
    public String determineDepartment(String category, String title, String description) {
        String cat = category != null ? category.trim().toLowerCase() : "";
        String text = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        // 1. Exact Category Checks
        if (cat.contains("road") || cat.contains("pothole") || cat.contains("street road")) {
            return DEPT_ROADS;
        }
        if (cat.contains("water")) {
            return DEPT_WATER;
        }
        if (cat.contains("waste") || cat.contains("garbage") || cat.contains("sanitation")) {
            return DEPT_SANITATION;
        }
        if (cat.contains("light") || cat.contains("electric") || cat.contains("power")) {
            return DEPT_ELECTRICITY;
        }
        if (cat.contains("drain") || cat.contains("sewage")) {
            return DEPT_DRAINAGE;
        }
        if (cat.contains("safety") || cat.contains("security") || cat.contains("police")) {
            return DEPT_PUBLIC_SAFETY;
        }

        // 2. Keyword fallback in title / description
        if (text.contains("pothole") || text.contains("tar road") || text.contains("road damaged") || text.contains("asphalt") || text.contains("flyover") || text.contains("footpath")) {
            return DEPT_ROADS;
        }
        if (text.contains("water leakage") || text.contains("pipeline") || text.contains("water supply") || text.contains("no water") || text.contains("tap water")) {
            return DEPT_WATER;
        }
        if (text.contains("garbage") || text.contains("trash") || text.contains("dustbin") || text.contains("waste disposal") || text.contains("cleanliness")) {
            return DEPT_SANITATION;
        }
        if (text.contains("street light") || text.contains("streetlight") || text.contains("lamp") || text.contains("power cut") || text.contains("power") || text.contains("transformer") || text.contains("electric")) {
            return DEPT_ELECTRICITY;
        }
        if (text.contains("drainage") || text.contains("clogged drain") || text.contains("sewer") || text.contains("manhole")) {
            return DEPT_DRAINAGE;
        }
        if (text.contains("public safety") || text.contains("encroachment") || text.contains("crime") || text.contains("hazard")) {
            return DEPT_PUBLIC_SAFETY;
        }

        return DEPT_GENERAL;
    }

    public List<String> getAllDepartments() {
        return ALL_DEPARTMENTS;
    }
}
