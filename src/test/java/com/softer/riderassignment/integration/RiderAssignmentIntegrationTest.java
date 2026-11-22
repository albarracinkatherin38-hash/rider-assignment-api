package com.softer.riderassignment.integration;

import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.RiderAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderAssignmentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RiderAssignmentRepository repository;

    @Test
    @Order(1)
    @DisplayName("POST /api/assignments registra una nueva asignación")
    @Sql(statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldCreateAssignment() {
        Map<String, Object> request = Map.of(
                "deliveryId", 7001,
                "riderId", 301,
                "status", "ASSIGNED"
        );

        ResponseEntity<RiderAssignment> response =
                restTemplate.postForEntity("/api/assignments", request, RiderAssignment.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(7001L, response.getBody().getDeliveryId());
        assertEquals(301L, response.getBody().getRiderId());
        assertEquals(AssignmentStatus.ASSIGNED, response.getBody().getStatus());
        assertEquals(1L, repository.count());
    }
}
