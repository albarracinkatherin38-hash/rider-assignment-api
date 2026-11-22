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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Test
    @Order(2)
    @DisplayName("GET /api/assignments retorna lista vacía cuando no hay asignaciones")
    void shouldReturnEmptyListWhenThereAreNoAssignments() {
        ResponseEntity<RiderAssignment[]> response =
                restTemplate.getForEntity("/api/assignments", RiderAssignment[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/assignments retorna las asignaciones existentes")
    @Sql(statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', '2026-11-22 10:00:00')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldReturnAllAssignmentsWhenThereAreData() {
        ResponseEntity<RiderAssignment[]> response =
                restTemplate.getForEntity("/api/assignments", RiderAssignment[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(7001L, response.getBody()[0].getDeliveryId());
        assertEquals(301L, response.getBody()[0].getRiderId());
        assertEquals(AssignmentStatus.ASSIGNED, response.getBody()[0].getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/assignments/{id} retorna la asignación solicitada")
    @Sql(statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', '2026-11-22 10:00:00')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldReturnAssignmentById() {
        ResponseEntity<RiderAssignment> response =
                restTemplate.getForEntity("/api/assignments/1", RiderAssignment.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(7001L, response.getBody().getDeliveryId());
        assertEquals(301L, response.getBody().getRiderId());
        assertEquals(AssignmentStatus.ASSIGNED, response.getBody().getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/assignments/{id} actualiza la asignación")
    @Sql(statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) "
            + "VALUES (1, 7001, 301, 'ASSIGNED', '2026-11-22 10:00:00')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldUpdateAssignment() {
        Map<String, Object> request = Map.of(
                "deliveryId", 7001,
                "riderId", 999,
                "status", "ACCEPTED"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<RiderAssignment> response = restTemplate.exchange(
                "/api/assignments/1",
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                RiderAssignment.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(7001L, response.getBody().getDeliveryId());
        assertEquals(999L, response.getBody().getRiderId());
        assertEquals(AssignmentStatus.ACCEPTED, response.getBody().getStatus());
    }
}
