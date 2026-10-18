package com.agileflow.api.controller;

import com.agileflow.api.dto.request.AuthRequest;
import com.agileflow.api.dto.request.TaskCreateRequest;
import com.agileflow.api.dto.request.TaskStatusUpdateRequest;
import com.agileflow.api.dto.response.AuthResponse;
import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        AuthRequest loginRequest = new AuthRequest("admin", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            AuthResponse.class
        );
        this.adminToken = "Bearer " + authResponse.accessToken();
    }

    @Test
    @DisplayName("GET /api/tasks - should return 403 Forbidden when unauthenticated")
    void getTasks_ShouldReturnForbidden_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/tasks - should return 200 OK and paginated tasks when authenticated")
    void getTasks_ShouldReturnTasks_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/tasks - should create new task and return 201 Created")
    void createTask_ShouldReturnCreated_WhenValidPayload() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
            "Configure 5G Network Slice Management",
            "Define QoS profile parameters",
            TaskStatus.TODO,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(5),
            null
        );

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.title", is("Configure 5G Network Slice Management")))
            .andExpect(jsonPath("$.creator.username", is("admin")));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status - should advance status")
    void updateTaskStatus_ShouldUpdateStatus() throws Exception {
        TaskStatusUpdateRequest statusRequest = new TaskStatusUpdateRequest(TaskStatus.DONE);

        mockMvc.perform(patch("/api/tasks/1/status")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("DONE")));
    }
}
