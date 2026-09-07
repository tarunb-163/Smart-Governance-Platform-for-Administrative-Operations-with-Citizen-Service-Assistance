package com.civicpulse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentRoutingServiceTest {

    private DepartmentRoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new DepartmentRoutingService();
    }

    @Test
    void testRouteByCategory() {
        assertEquals("Roads / Public Works Department", routingService.determineDepartment("Road Damage", "Broken asphalt", ""));
        assertEquals("Water Department", routingService.determineDepartment("Water Supply", "No water pressure", ""));
        assertEquals("Sanitation Department", routingService.determineDepartment("Garbage Collection", "Trash overflowing", ""));
        assertEquals("Electricity Department", routingService.determineDepartment("Streetlight Issue", "Light pole dark", ""));
        assertEquals("Public Works / Sanitation Department", routingService.determineDepartment("Drainage Problem", "Blocked storm drain", ""));
        assertEquals("Public Safety Department", routingService.determineDepartment("Safety Hazard", "Open manhole cover", ""));
    }

    @Test
    void testRouteByKeywordWhenCategoryIsOther() {
        assertEquals("Roads / Public Works Department", routingService.determineDepartment("Other", "Massive pothole on highway 101", ""));
        assertEquals("Water Department", routingService.determineDepartment("General", "Pipeline leak flooding the lane", ""));
        assertEquals("Electricity Department", routingService.determineDepartment("Other", "Streetlight blinking continuously", ""));
        assertEquals("Sanitation Department", routingService.determineDepartment("Other", "Garbage dump accumulating near market", ""));
    }

    @Test
    void testDefaultFallback() {
        assertEquals("General Administration", routingService.determineDepartment("Unknown Category", "Something unspecified", ""));
    }
}
